FROM node:24-alpine

WORKDIR /app
COPY . .

# add bash
RUN apk add --no-cache bash

RUN chmod +x /app/install.sh

USER node

EXPOSE 8080
