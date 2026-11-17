FROM nginx:1.30-alpine
COPY docker/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY docker/nginx/static/ /usr/share/nginx/html/static/
COPY docs/ /usr/share/nginx/html/documentation/
COPY docs/img/screen-sketches.svg /usr/share/nginx/html/static/img/screen-sketches.svg
