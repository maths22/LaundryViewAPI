# specify the node base image with your desired version node:<version>
FROM node:8
# replace this with your application's default port
EXPOSE 80

FROM node:carbon

# Create app directory
WORKDIR /usr/src/app

# Install app dependencies
# A wildcard is used to ensure both package.json AND package-lock.json are copied
# where available (npm@5+)
COPY package*.json ./

RUN npm install --only=production

# Bundle app source
COPY lib lib
# Include service account key
COPY private private

ENV PORT=80
CMD [ "node", "lib/server.js" ]

