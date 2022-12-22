import os
import json
import shutil
import zipfile
from flask import Flask, request, jsonify, send_file, render_template, url_for, redirect
from werkzeug.utils import secure_filename
from datetime import timedelta
from flask_sockets import Sockets
from pydub.audio_segment import AudioSegment

# 创建flask实例对象
app = Flask(__name__)
sockets = Sockets(app)

# 全局配置
UPLOAD_DIR = './templates/'
ALLOWED_EXT = ('wav')
path = './static/upload/'
model = 'C:/dnndenoiser/denoiser_server/looking-to-listen-master/data/model/0f_1sclean_noise.npz'
path_clean = "C:/dnndenoiser/denoiser_server/static/clean/"

# 设置静态文件缓存过期时间
app.send_file_max_age_default = timedelta(seconds=60)

# 判断文件后缀名


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXT
    #  rsplit() 方法通过指定分隔符对字符串进行分割并返回一个列表

# 压缩文件为zip格式


def zip_file(src_dir):
    zip_name = src_dir + '.zip'
    z = zipfile.ZipFile(zip_name, 'w', zipfile.ZIP_DEFLATED)
    for dirpath, dirnames, filenames in os.walk(src_dir):
        fpath = dirpath.replace(src_dir, '')
        fpath = fpath and fpath + os.sep or ''
        for filename in filenames:
            z.write(os.path.join(dirpath, filename), fpath + filename)
            print('==success==')
    z.close()

@sockets.route('/recieve/<username>')
async def test(ws, username):
    await ws.accept()
    data1 = bytes()
    data2 = bytes()

    while True:
        try:
            path_user = path + username + '/'
            path_user_clean=path_clean + username + '/'
            if not os.path.exists(path_user):
                os.makedirs(path_user)
            data1 = await ws.receive_bytes()
            if not os.path.exists(path_user_clean):
                os.makedirs(path_user_clean)
            data1 = await ws.receive_bytes()
            # print(data)
            data2 = data2 + data1
            # data=data+data1
            # data2=data1

            # sample_width是需要2字节，而不是多少位16
            # data=data-data2
            audiosegment2 = AudioSegment(data=data2, sample_width=2, frame_rate=8000,
                                         channels=1)
            filename2 = username + '.wav'
            # 拼接文件路径
            # file_path2 = os.path.join(filename2)
            file_path2 = path_user + filename2

            audiosegment2.export(file_path2, format='wav')
        except Exception as e:
            print('error:', e)
            break   
    return redirect(url_for('/processing/' + username))

@sockets.route('/send/<username>')
def send(ws,username):
    ws.send(path_clean+'/'+username+".zip")
    shutil.rmtree(path_clean + username)
    shutil.rmtree(path_clean + username+'.zip')

# # 路由注册
# @app.route('/', methods=['GET', 'POST'])
# def index():
#     # 将静态文件从静态文件夹发送到浏览器
#     return app.send_static_file('index.html')

@app.route('/')
def helloworld():
    return 'HelloWorld'

# 文件上传
@app.route('/processing/<username>')
def upload(username):
    path_user=path+username+'/'
    # 图片展示
    files = os.listdir(path_user)
    fileList = {}
    for file in files:
        file_d = os.path.join(path_user, file)
        # 执行模型脚本
        res = os.popen("python ./looking-to-listen-master/network/src/denoiser_server.py C:/dnndenoiser/denoiser_server/looking-to-listen-master/data/model/0f_1sclean_noise.npz %s" % file_d+' '+username)
        labels = res.read()  # 形成阻塞效果，使另一通道中程序运行完再执行下一句
        label = str(labels).strip('\n')
        if label in fileList.keys():
            fileList[label].append({"filename": file, "path": file_d})
        else:
            fileList[label] = [{"filename": file, "path": file_d}]
        # 将字典形式的数据转化为字符串
    path_user_clean=path_clean+username
    zip_file(path_user_clean)
    shutil.rmtree(path + username)
    # return send_file(path_clean+'/'+username+".zip")
    return redirect(url_for('/send/' + username))
    # return render_template('success.html')


if __name__ == '__main__':
    # from gevent import pywsgi
    # from geventwebsocket.handler import WebSocketHandler
    # server = pywsgi.WSGIServer(('', 5000),  app, handler_class=WebSocketHandler)
    from SimpleWebSocketServer import SimpleWebSocketServer
    from SimpleWebSocketServer.SimpleWebSocketServer import WebSocket
    server = SimpleWebSocketServer('', self.port, ElevWSHandler, selectInterval=1)
    print('server start')
    server.serve_forever()

    # app.run(host="0.0.0.0", debug=True)
