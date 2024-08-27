FROM mongo
COPY mongodb-keyfile /data/replica.key
RUN chown 999:999 /data/replica.key && chmod 400 /data/replica.key
