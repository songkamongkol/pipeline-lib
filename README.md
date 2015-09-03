
# Docker Registry Proxy Configuration (Draft)
----

This repo contains configuration files and instruction to create docker registry proxy/cache server



## Docker registry proxy Components
Base OS: debian jessie

* squid ([squid3](http://http://www.squid-cache.org/))
* [nginx](http://nginx.org/en/)

## Docker Proxy Setup
----

1. Install squid

        apt-get install squid3


2. Replace */etc/squid3/squid.conf* with [squid.conf](https://github.com/songkamongkol/orion-salt/blob/corp/squid.conf)


3. Install nginx
        
        apt-get install nginx

4. Replace */etc/nginx.conf* with [nginx.conf](https://github.com/songkamongkol/orion-salt/blob/corp/nginx.conf)

5. Copy the cert and key files from the registry server and place them in */etc/ssl/private/cache.crt* and */etc/ssl/private/cache.key* respectively

6. Restart nginx and squid3

        systemctl restart squid3
        systemctl restart nginx


## Registry Server update
Restart registry container (registry:v2) and ensure that */etc/docker/registry/config.yml* has the same content as this [config.yml](https://github.com/songkamongkol/orion-salt/blob/corp/config.yml) (via volume expose or config file variable overrides) 

>*NOTE:* **redirect** *must be disabled*

## Docker client Setup (For Testing)
1. Add a line in */etc/hosts* to direct DNS name of the registry server to the IP of the proxy server 

        10.180.106.17   registry.oriontest.net


2. To pull an image, use the following command

        docker pull registry.oriontest.net/traffic-center

>*Note: in the production environment, step 1 will be handled by Route53*
