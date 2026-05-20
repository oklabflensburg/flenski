FROM node:24-alpine

RUN mkdir -p /app/flenski && chown -R node:node /app/flenski

WORKDIR /app/flenski

COPY ./package*.json ./

RUN apk add --no-cache bash

RUN npm install

RUN mkdir -p /app/flenski/node_modules && chown -R node:node /app/flenski/node_modules
RUN chown -R node:node /app

USER node

EXPOSE 8080

CMD npm install && npm run dev
