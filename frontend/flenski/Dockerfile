FROM node:24-alpine

WORKDIR /app
COPY . .

# add bash
RUN apk add --no-cache bash

RUN npm install

RUN chown -R node:node /app

USER node

EXPOSE 8080
