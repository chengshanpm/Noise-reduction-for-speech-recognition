# app.py
import os
from pydub.audio_segment import AudioSegment

import uvicorn
from fastapi import FastAPI, WebSocket
app = FastAPI()

@app.websocket("/")

async def test(websocket: WebSocket):
    await websocket.accept()
    data=bytes()
    data1=bytes()
    data2=bytes()

    while True:
        try:
            data=data+data1
            data1 = await websocket.receive_bytes()
            #print(data)
            print(len(data1))
            data2=data2+data1
            #data=data+data1
            #data2=data1

            #sample_width是需要2字节，而不是多少位16
            #data=data-data2
            audiosegment = AudioSegment(data=data, sample_width=2, frame_rate=8000,
                                       channels=1)
            audiosegment2 = AudioSegment(data=data2, sample_width=2, frame_rate=8000,
                                       channels=1)
            filename1 = 'test_file' + '.wav'
            filename2 = 'test_file2' + '.wav'
            #拼接文件路径
            file_path1 = os.path.join(filename1)
            file_path2 = os.path.join(filename2)

            audiosegment.export(file_path1,format='wav')
            audiosegment2.export(file_path2,format='wav')
        except Exception as e:
            print('error:',e)
            break


        
        
if __name__ == "__main__":
    uvicorn.run("test:app",host="101.200.61.248",port=8080)


 

 

 
