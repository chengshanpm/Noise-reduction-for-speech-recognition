import os
import wave
import math
import glob
import shlex
import shutil
import argparse
import subprocess
import sys

import chainer

import env
import common.util as util
import common.settings as settings
import modules.operation as op
from network.audio_only_net import Audio_Only_Net
import sounddevice as sd


xp = env.xp
model = None
ideep_mode = 'never'


def masking(audios):

    if env.gpu:
        audios = xp.asarray(audios)

    noise = xp.stack([audio.T for audio in audios]).astype(xp.float32)
    compressed_noise, _ = op.compress_audio(noise)

    print("estimate mask...")
    mask1, mask2 = model.estimate_mask(spec=compressed_noise)

    print("mul mask...")
    compressed_separated1 = op.mul(mask1, compressed_noise).data
    compressed_separated2 = op.mul(mask2, compressed_noise).data

    if env.gpu:
        compressed_separated1 = chainer.cuda.to_cpu(compressed_separated1)
        compressed_separated2 = chainer.cuda.to_cpu(compressed_separated2)

    print("reconstruct audio...")
    y1 = op.reconstruct_audio_complex(compressed_separated1)
    y2 = op.reconstruct_audio_complex(compressed_separated2)

    return y1, y2


def predict(audios):

    with chainer.using_config("train", False), \
            chainer.using_config('enable_backprop', False), \
            chainer.using_config('type_check', False), \
            chainer.using_config('use_ideep', ideep_mode):
        y1, y2 = masking(audios)

    return y1, y2


def noise_reduction(wav_path, save_path):

    save_dir = save_path + "/noise/segments"
    if os.path.exists(save_dir):
        shutil.rmtree(save_dir)
    os.makedirs(save_dir)

    wf = wave.open(wav_path, "r")
    sec = float(wf.getnframes()) / wf.getframerate()
    wf.close()
    del wf

    segment_time = 3
    iteraction_count = int(math.ceil(sec / segment_time))

    for i in range(iteraction_count):
        start_time = i * segment_time
        path = save_dir + "/{0:02d}.wav".format(i)
        # with open(os.devnull, 'wb') as devnull:
        cmd = 'ffmpeg -ss {0} -t {1} -ar {2} -ac 1 -y {3} -i {4}'.format(
            start_time, segment_time, settings.SR, path, wav_path)
        # subprocess.call(shlex.split(cmd), stdout=devnull, stderr=devnull)
        subprocess.call(cmd.split(), shell=True)

    segments = sorted(glob.glob(save_dir + "/*"))

    save_dir = save_path + "/noise/denoise_segments"
    if os.path.exists(save_dir):
        shutil.rmtree(save_dir)
    os.makedirs(save_dir)

    audios = [util.load_audio_and_stft(segment) for segment in segments]
    y1, y2 = predict(audios)

    for i in range(y1.shape[2]):
        util.istft_and_save("{0}/{1:02d}.wav".format(save_dir, i), y1[:, :, i])

    segments = sorted(glob.glob(save_dir + "/*"))

    save_dir = save_path + "/noise"
    with open(save_dir + "/files.txt", "w") as f:
        for segement in segments:
            f.write("file \'" + segement + "\'\n")

    # with open(os.devnull, 'wb') as devnull:
    cmd = 'ffmpeg -ac 1 -safe 0 -f concat -ss 0 -t {0} -y -i {1} {2}'.format(
        round(sec, 2),
        save_dir + "/files.txt",
        save_path + "/clean.wav")
    subprocess.call(cmd.split(), shell=True)


def parse_audio_device(device):
    if device is None:
        return device
    try:
        return int(device)
    except ValueError:
        return device


def query_devices(device, kind):
    try:
        caps = sd.query_devices(device, kind=kind)  # sd.query_devices()显示系统所有的声音设备。
    except ValueError:
        message = "Invalid audio interface.\n"
        print(message)
        sys.exit(1)
    return caps


def main(args):
    global model, ideep_mode

    model = Audio_Only_Net()
    chainer.serializers.load_npz(args.model, model)
    print("Model loaded.")

    # 定义输入
    device_in = parse_audio_device(args.in_)
    caps = query_devices(device_in, "input")
    channels_in = min(caps['max_input_channels'], 2)
    stream_in = sd.InputStream(
        device=device_in,
        samplerate=model.sample_rate,
        channels=channels_in)

    # 定义输出
    device_out = parse_audio_device(args.out)
    caps = query_devices(device_out, "output")
    channels_out = min(caps['max_output_channels'], 2)
    stream_out = sd.OutputStream(
        device=device_out,
        samplerate=model.sample_rate,
        channels=channels_out)

    if env.gpu:
        model.to_gpu(args.g)
    elif args.ideep:
        model.to_intel64()

    ideep_mode = 'always' if args.ideep else 'never'

    stream_in.start()  # .start()启动线程
    stream_out.start()
    first = True
    current_time = 0
    last_log_time = 0
    last_error_time = 0
    cooldown_time = 2
    log_delta = 10
    sr_ms = model.sample_rate / 1000
    stride_ms = streamer.stride / sr_ms
    print(f"Ready to process audio, total lag: {streamer.total_length / sr_ms:.1f}ms.")
    while True:
        try:
            if current_time > last_log_time + log_delta:
                last_log_time = current_time
                tpf = streamer.time_per_frame * 1000
                rtf = tpf / stride_ms
                print(f"time per frame: {tpf:.1f}ms, ", end='')
                print(f"RTF: {rtf:.1f}")
                streamer.reset_time_per_frame()

            length = streamer.total_length if first else streamer.stride
            first = False
            current_time += length / model.sample_rate
            frame, overflow = stream_in.read(length)
            frame = torch.from_numpy(frame).mean(dim=1).to(args.device)  # from_numpy-生成张量，mean-输出平均值(dim=1行平均，dim=0列平均)，to()转到设备***
            with torch.no_grad():
                out = predict(frame[None])[0]
            out = out[:, None].repeat(1, channels_out)
            mx = out.abs().max().item()
            if mx > 1:
                print("Clipping!!")
            out.clamp_(-1, 1)
            out = out.cpu().numpy()
            underflow = stream_out.write(out)
            if overflow or underflow:
                if current_time >= last_error_time + cooldown_time:
                    last_error_time = current_time
                    tpf = 1000 * streamer.time_per_frame
                    print(f"Not processing audio fast enough, time per frame is {tpf:.1f}ms "
                          f"(should be less than {stride_ms:.1f}ms).")
        except KeyboardInterrupt:
            print("Stopping")
            break
    stream_out.stop()
    stream_in.stop()


if __name__ == "__main__":
    """ main endpoint """

    parser = argparse.ArgumentParser(
        description='description'
    )
    parser.add_argument("model", help="model or snapshot")
    parser.add_argument(
        "-i", "--in", dest="in_",
        help="name or index of input interface.")
    parser.add_argument(
        "-o", "--out", default="Soundflower (2ch)",
        help="name or index of output interface.")
    parser.add_argument("--ideep", action='store_true', help="ideep (CPU Only)")
    parser.add_argument("-g", type=int, default=0, help="specify GPU")
    args = parser.parse_args()

    main(args)
