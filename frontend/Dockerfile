FROM node:24-alpine

WORKDIR /app/flenski

COPY flenski/package*.json ./

# add bash
RUN apk add --no-cache bash

RUN npm install

RUN chown -R node:node /app

# Copy existing application directory permissions
COPY --chown=www:www . /var/www

# Change current user to node
USER node

EXPOSE 8080

CMD npm install && npm run format && npm run dev
