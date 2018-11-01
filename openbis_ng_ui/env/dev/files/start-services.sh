sudo /usr/sbin/VBoxService --timesync-set-start

sudo mount --bind /home/vagrant/openbis-build /home/vagrant/openbis/openbis_ng_ui/node
sudo mount --bind /home/vagrant/openbis-build/node_modules /home/vagrant/openbis/openbis_ng_ui/node_modules

cd openbis/openbis_ng_ui
./gradlew --gradle-user-home /home/vagrant/openbis-build --project-cache-dir /home/vagrant/openbis-build npmSetup
export PATH=$PATH:/home/vagrant/openbis-build/nodejs/node-v10.1.0-linux-x64/bin
node /home/vagrant/openbis-build/nodejs/node-v10.1.0-linux-x64/bin/npm install
cd 

screen -S dev -t webpack -Adm bash -c "cd openbis/openbis_ng_ui; export PATH=$PATH:/home/vagrant/openbis-build/nodejs/node-v10.1.0-linux-x64/bin; node /home/vagrant/openbis-build/nodejs/node-v10.1.0-linux-x64/bin/npm run dev; bash"

echo "Waiting Webpack to launch on 8124..."

while ! nc -z localhost 8124; do
  sleep 3
done
