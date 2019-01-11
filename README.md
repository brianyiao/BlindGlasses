# BlindGlasses

## 環境安裝
**作業系統：Ubuntu 16.04**
### Android Studio
(1)到官網下載Android Studio安裝檔(https://developer.android.com/studio/) 

![image](https://github.com/brianyiao/BlindGlasses/blob/master/01.JPG)

(2)打勾後按「DOWNLOAD ANDROIDSTUDIO FOR WINDOWS」 

![image](https://github.com/brianyiao/BlindGlasses/blob/master/02.JPG)

(3)按「Next>」 

![image](https://github.com/brianyiao/BlindGlasses/blob/master/04.JPG)

(4)按「Next>」 

![image](https://github.com/brianyiao/BlindGlasses/blob/master/05.JPG)

(5)按「Next>」 

![image](https://github.com/brianyiao/BlindGlasses/blob/master/06.JPG)  

(6)按「Install」 

![image](https://github.com/brianyiao/BlindGlasses/blob/master/07.JPG)

(7)按「Next>」 

![image](https://github.com/brianyiao/BlindGlasses/blob/master/09.JPG)  

(8)按「Finish」 

![image](https://github.com/brianyiao/BlindGlasses/blob/master/10.JPG)

### Nvidia驅動與cuda9.0安裝
(1)sudo add-apt-repository ppa:graphics-drivers/ppa -y
(2)sudo apt-get update
(3)sudo apt-get upgrade -y
(4)sudo wget https://developer.nvidia.com/compute/cuda/9.0/Prod/local_installers/cuda-repo-ubuntu1604-9-0-local_9.0.176-1_amd64-deb
(5)sudo dpkg -i cuda-repo-ubuntu1604-9-0-local_9.0.176-1_amd64.deb
(6)sudo apt-key add /var/cuda-repo-9-0-local/7fa2af80.pub
(7)sudo apt-get update
(8)sudo apt-get install cuda –y
(9)sudo gedit ~/.bashrc
加入環境變數
export PATH=/usr/local/cuda-9.0/bin${PATH:+:${PATH}}
export LD_LIBRARY_PATH=/usr/local/cuda-9.0/lib64${LD_LIBRARY_PATH:+:${LD_LIBRARY_PATH}}
(10)source ~/.bashrc
重開機

### Cudnn7.0.5安裝
(1)wget https://developer.nvidia.com/compute/machine-learning/cudnn/secure/v7.0.5/prod/9.0_20171129/cudnn-9.0-linux-x64-v7
(2)tar -xvf cudnn-9.0-linux-x64-v7.tgz
(3)sudo cp cuda/include/cudnn.h /usr/local/cuda/include/
(4)sudo cp cuda/lib64/libcudnn* /usr/local/cuda/lib64/
(5)sudo chmod a+r /usr/local/cuda/include/cudnn.h
(6)sudo chmod a+r /usr/local/cuda/lib64/libcudnn*

### Anaconda安裝
(1)wget https://repo.anaconda.com/archive/Anaconda3-5.2.0-Linux-x86_64.sh
(2)bash Anaconda3-5.2.0-Linux-x86_64.sh –b
(3)sudo gedit ~/.bashrc
加入環境變數
export PATH=“/home/使用者名稱/anaconda3/bin:$PATH”
(4)source ~/.bashrc

### Tensorflow-gpu1.5.0與keras2.1.3安裝
(1)pip install tensorflow-gpu==1.5.0
(2)pip install keras==2.1.3 

### Opencv與h5py安裝
pip install opencv-contrib-python
pip install h5py

### Cuda與Cudnn版本查看
cat /usr/local/cuda/version.txt                             
cat /usr/local/cuda/include/cudnn.h | grep CUDNN_MAJOR -A 2
