# Noise-reduction-for-speech-recognition
Audio noise reduction for speech recognition applications

## Noise reduction algorithm
### Existing noise reduction models
#### Demucs model
##### Introduction
[Demucs Model](https://github.com/facebookresearch/denoiser) is Facebook's open source sound separation model. This is a speech enhancement model that runs in real time on a laptop computer with a CPU. The model is an Encoder-decoder architecture with Skip-Connection structure. Multiple loss functions are used to optimize both time domain and frequency domain. Experimental results show that this method can remove all kinds of background noise, including stationary noise and non-stationary noise, as well as indoor reverberation. It has high accuracy and effectiveness.

The structure is shown below

<div align=center><img src="https://github.com/chengshanpm/Noise_reduction_for_speech_recognition/blob/main/images/demucs.png" width="400" height="400" /></div>

##### Training and Results

We trained the model using two datasets, Valentini and DNS64. The results of the two datasets have their own characteristics. In the case of light noise, the pre-trained models with DNS64 and Valentini datasets have similar effects, but in the case of strong noise, the pre-trained models with DNS64 are obviously better than those with Valentini datasets. In the strong noise environment, the human voice content can hardly be distinguished in the output audio of the model trained with Valentini dataset.

According to this model we used audio to test, the results are as follows:

* Original sound

https://user-images.githubusercontent.com/94374226/191673493-d94e8ac5-502c-43e4-adde-9cc6a4ae781a.mp4

https://user-images.githubusercontent.com/94374226/191673521-4e186c2c-1157-481c-948c-f8e7bfce4020.mp4

* DNS64 traning result

https://user-images.githubusercontent.com/94374226/191673610-8c7bbd4a-9fec-47a1-b4e5-28fb994b4584.mp4

https://user-images.githubusercontent.com/94374226/191673619-a54c433e-b480-4a17-aa8c-85de3005f509.mp4

* Valentini traning result

https://user-images.githubusercontent.com/94374226/191673657-d4d0ea47-8f90-4a28-bf31-95261a4ddf9d.mp4

https://user-images.githubusercontent.com/94374226/191673670-bc68b5ee-b64f-44ba-9f1e-28ed9bb940ec.mp4

#### Speech separation model based on DRNN
The general framework is as follows:
<div align=center><img src="https://github.com/chengshanpm/Noise_reduction_for_speech_recognition/blob/main/images/proposed%20frame.png" width="600" height="200" /></div>

The phase spectrum and amplitude spectrum of mixed signal with noise are obtained by Fourier transform. Using the amplitude spectrum of the mixed signal as the input features, deep recurrent neural network joint discrimination training is performed to obtain Esitmated amplitude Spectra of human voice and noise. The amplitude Spectra of human voice and noise and phase Spectra of the mixed signal are transformed by short-time inverse Fourier transform. The separated waveform spectra of human voice and noise were obtained. Finally evaluate the results. The main content of DNN/DRNN in single channel music voice separation is in the dashed box. Among them, Time Frequency Masking is a commonly used technique in human voice separation, while Discriminative Training is a loss function used by DNN/DRNN in Training human voice separation models.

The DRNN(Deep Recurrent Neural Network) structure is as follows:
<div align=center><img src="https://github.com/chengshanpm/Noise_reduction_for_speech_recognition/blob/main/images/DRNN%20structure.png" width="400" height="400" /></div>

#### Speech separation model based on DNN
DNN is adopted as a regression model to predict the log-power spectral features of the target speaker given the input log-power spectral features of mixed speech with acoustic context, which is shown in Fig. The log-power spectral features can offer perceptually relevant parameters. The acoustic context information along both time axis (with multiple neighboring frames) and frequency axis (with full frequency bins) can be fully utilized by DNN to improve the continuity of estimated clean speech while the conventional GMM-based approach do not model the temporal dynamics of speech.
<div align=center><img src="https://github.com/chengshanpm/Noise_reduction_for_speech_recognition/blob/main/images/dnn.jpeg" width="400" height="400" /></div>

##### Training and Results
In the previous attempts to solve the noise separation problem with demucs, excessive noise reduction often occurred under the condition of strong noise, that is, part of the human voice was also removed, resulting in the problem of human voice discontinuity in the results. In the results obtained by the DNN-based speech separation algorithm, the discontinuity of human voice has been greatly improved, and the sound quality of human voice has also been improved, and the corresponding accuracy of speech recognition has also been improved.

According to this model audio test results are as follows:

https://user-images.githubusercontent.com/94374226/195542389-62a6c02e-9673-4f43-a671-c386d7bd6e30.mp4

https://user-images.githubusercontent.com/94374226/195542412-bd8d6c0b-d9d9-473c-833d-d4d125262e71.mp4

### Our noise reduction model



## Connect to iFlytek Translate database through API



## Adaptation of mobile phones and other devices
We develop software for Android phones based on the Android Studio platform. This software mainly realizes real-time speech noise reduction and real-time speech recognition. By turning on and off the noise reduction button, we can observe obvious differences in speech recognition accuracy, so as to judge our noise reduction effect.

The recognition language is only English at present. There are two interfaces, respectively as follows:

If recording is not started, click the record button to jump to the recording screen.
<div align=center><img src="https://github.com/chengshanpm/Noise_reduction_for_speech_recognition/blob/main/images/Not%20recorded.png" width="200" height="400" /></div>

Screen in recording, click the button to jump to the screen that has not started recording.
<div align=center><img src="https://github.com/chengshanpm/Noise_reduction_for_speech_recognition/blob/main/images/In%20the%20recording.png" width="200" height="400" /></div>


