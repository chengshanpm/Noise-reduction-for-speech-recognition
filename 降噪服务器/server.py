import os
import json
import shutil
import zipfile
from flask import Flask, request, jsonify, send_file
from werkzeug.utils import secure_filename

# 创建flask实例对象
app = Flask(__name__)

# 全局配置
UPLOAD_DIR = './templates/'
ALLOWED_EXT = ('wav')
path = './static/upload/'
model='C:/dnndenoiser/HelloWorld/looking-to-listen-master/data/model/0f_1sclean_noise.npz'
path_clean = "C:/dnndenoiser/HelloWorld/static/clean"

# 判断文件后缀名
def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXT
    #  rsplit() 方法通过指定分隔符对字符串进行分割并返回一个列表

# 压缩文件为zip格式
def zip_file(src_dir):
    zip_name = src_dir +'.zip'
    z = zipfile.ZipFile(zip_name,'w',zipfile.ZIP_DEFLATED)
    for dirpath, dirnames, filenames in os.walk(src_dir):
        fpath = dirpath.replace(src_dir,'')
        fpath = fpath and fpath + os.sep or ''
        for filename in filenames:
            z.write(os.path.join(dirpath, filename),fpath+filename)
            print ('==success==')
    z.close()


# 路由注册
@app.route('/', methods=['GET', 'POST'])
def index():
    # 将静态文件从静态文件夹发送到浏览器
    return app.send_static_file('index.html')


# 文件上传
@app.route('/upload/', methods=['POST'])
def upload():
    if request.method == 'POST':
        f = request.files['file']
        if not (f and allowed_file(f.filename)):
            return jsonify({"error": 1001, "msg": "请检查上传的文件类型，仅限于wav"})
        # upload_path = os.path.join(path, secure_filename(f.filename))  
        upload_path = os.path.join(path, str(f.filename))     #secure_filename()过滤掉文件名中的不合法字符
        f.save(upload_path)

        # 图片展示
        files = os.listdir(path)
        fileList = {}
        for file in files:
            file_d = os.path.join(path, file)
            # 执行模型脚本
            res = os.popen("python ./looking-to-listen-master/network/src/server_test.py C:/dnndenoiser/HelloWorld/looking-to-listen-master/data/model/0f_1sclean_noise.npz %s" % file_d)
            labels = res.read()     #形成阻塞效果，使另一通道中程序运行完再执行下一句
            label = str(labels).strip('\n')
            if label in fileList.keys():
                fileList[label].append({"filename": file, "path": file_d})
            else:
                fileList[label] = [{"filename": file, "path": file_d}]
            # 将字典形式的数据转化为字符串
        zip_file(path_clean)
    return send_file("C:/dnndenoiser/HelloWorld/static/clean.zip")

if __name__ == '__main__':
    app.run(host="0.0.0.0", debug=True)