FROM node:9.11.2-alpine

EXPOSE 3000

COPY . .

RUN yarn install

CMD yarn start

# do not generate and serve the production build to enable MockApi
# RUN yarn build
# RUN yarn global add serve
# CMD serve -s build
