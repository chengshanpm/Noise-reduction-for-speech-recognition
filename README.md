# Noise-reduction-for-speech-recognition
Audio noise reduction for speech recognition applications

## Noise reduction algorithm
### Existing noise reduction models
#### Demucs model
[Demucs Model](https://github.com/facebookresearch/denoiser) is Facebook's open source sound separation model. This is a speech enhancement model that runs in real time on a laptop computer with a CPU. The model is an Encoder-decoder architecture with Skip-Connection structure. Multiple loss functions are used to optimize both time domain and frequency domain. Experimental results show that this method can remove all kinds of background noise, including stationary noise and non-stationary noise, as well as indoor reverberation. It has high accuracy and effectiveness.

The structure is shown below

<div align=center><img src="https://github.com/chengshanpm/Noise_reduction_for_speech_recognition/blob/main/images/demucs.png" width="400" height="400" /></div>

We trained the model using two datasets, Valentini and DNS64. The results of the two datasets have their own characteristics. In the case of light noise, the pre-trained models with DNS64 and Valentini datasets have similar effects, but in the case of strong noise, the pre-trained models with DNS64 are obviously better than those with Valentini datasets. In the strong noise environment, the human voice content can hardly be distinguished in the output audio of the model trained with Valentini dataset.

According to this model we used audio to test, the results are as follows:

**Original sound**


**DNS64 traning result**


**Valentini traning result**


### Our noise reduction model



## Connect to Google Translate database through API



## Adaptation of mobile phones and other devices



