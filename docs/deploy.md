# Deploying the service

## On a Ubuntu server

### Prepare the server

Install required ubuntu packages:
    
    sudo apt-get install apache2 mongodb openjdk-7-jre

Enable the proxy module in apache:

    sudo a2enmod proxy
    sudo a2enmod proxy_http

See the [example](apache2.md) for apache configuration.

## Prepare the application

Create a standalone version of the app.

    play universal:package-zip-tarball

Copy the resulting tarball to the server.

### Install the application

Extract the app and place it in the /opt/akvaario/ folder. 

    tar xzf akvaario-<version>.tgz

    sudo cp -R akvaario-<version> /opt/akvaario

Make sure the www-data user is the owner of all the files.

    sudo chown -R www-data:www-data /opt/akvaario


Place the init script in the /etc/init.d folder and make sure it is runnable.

    sudo cp akvaario.init /etc/init.d/akvaario && sudo chmod +x /etc/init.d/akvaario

