cvs log > /tmp/ac
cd /home/lokal/codeswarm-read-only/convert_logs
python convert_logs.py -c /tmp/ac -o ../data/me.xml
cd ..
bash run.sh

