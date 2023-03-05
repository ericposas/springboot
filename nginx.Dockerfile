FROM nginx:latest
COPY ./certs /etc/nginx/certs
COPY ./nginx/ssl-default.conf /etc/nginx/conf.d/default.conf
