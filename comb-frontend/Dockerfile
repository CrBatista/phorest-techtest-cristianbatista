# Stage 1: build the Angular application
FROM node:20-alpine AS build

WORKDIR /app

COPY package.json ./
RUN npm install

COPY angular.json tsconfig.json tsconfig.app.json tsconfig.spec.json karma.conf.js ./ 
COPY src ./src

RUN npm run build -- --configuration=production

# Stage 2: serve with Nginx
FROM nginx:1.27-alpine

COPY --from=build /app/dist/comb-as-you-are /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
