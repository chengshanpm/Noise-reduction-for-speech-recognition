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

**Original sound**

https://user-images.githubusercontent.com/94374226/191673493-d94e8ac5-502c-43e4-adde-9cc6a4ae781a.mp4

https://user-images.githubusercontent.com/94374226/191673521-4e186c2c-1157-481c-948c-f8e7bfce4020.mp4

**DNS64 traning result**

https://user-images.githubusercontent.com/94374226/191673610-8c7bbd4a-9fec-47a1-b4e5-28fb994b4584.mp4

https://user-images.githubusercontent.com/94374226/191673619-a54c433e-b480-4a17-aa8c-85de3005f509.mp4

**Valentini traning result**

https://user-images.githubusercontent.com/94374226/191673657-d4d0ea47-8f90-4a28-bf31-95261a4ddf9d.mp4

https://user-images.githubusercontent.com/94374226/191673670-bc68b5ee-b64f-44ba-9f1e-28ed9bb940ec.mp4

### Our noise reduction model



## Connect to Google Translate database through API



## Adaptation of mobile phones and other devices



