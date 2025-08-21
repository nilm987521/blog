FROM --platform=linux/arm64 nginx:stable-alpine3.21-perl
COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf
