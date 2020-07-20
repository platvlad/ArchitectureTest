# ArchitectureTest

Test different server architectures:

1) One thread per client connection

2) One thread for receiving and SingleThreadPoolExecutor for sending messages for every client. FixedThreadPool for incoming tasks

3) Non-blocking architecture

## Usage

1) Start ServerRunner

2) Enter server ip to file ip.txt

3) Start GUI
