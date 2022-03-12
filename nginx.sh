docker run -it --rm \
-p 9090:80 \
-v $(pwd)/nginx.conf:/etc/nginx/conf.d/default.conf \
nginx:1.21.6