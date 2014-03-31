# Apache2 configuration

The apache configuration for redirecting everything behind ssl should look something like this.
```
<VirtualHost *:80>
	ServerAdmin lifecycle@futurice.com
	RewriteEngine On
	RewriteCond %{HTTPS} off
	RewriteRule (.*) https://%{HTTP_HOST}%{REQUEST_URI} [L,NE,R=301]
</VirtualHost>
```

The proxy configuration with ssl should look something like this:
```
<IfModule mod_ssl.c>
 <VirtualHost *:443>
	ServerAdmin lifecycle@futurice.com
	
	# Pass forward that we are running over SSL
	RequestHeader set X-Forwarded-Proto "https"

	ProxyPreserveHost On
	ProxyRequests Off
	ProxyPass / http://127.0.0.1:9000/
	ProxyPassReverse / http://127.0.0.1:9000/

    SSLEngine on

    SSLCertificateFile /path/to.crt
    SSLCertificateKeyFile /path/to.key
    SSLCACertificateFile /path/to.crt
 </VirtualHost>
</IfModule>
```