import subprocess


cmd = ['ffmpeg', '-ss', '0', '-t', '3', '-ar', '16000', '-ac', '1', '-y',
       'C:/dnndenoiser/looking-to-listen-master/data/clean/20220915_114314/noise/segments/00.wav',
       '-i', 'C:/dnndenoiser/looking-to-listen-master/data/noise/20220915_114314.wav']
# print(cmd)

subprocess.call(cmd, shell=True)
