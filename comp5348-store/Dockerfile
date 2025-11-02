FROM node:18-alpine AS builder
WORKDIR /app

COPY package*.json ./
COPY package-lock.json ./
RUN npm ci --silent

COPY . .

ARG VITEAPIURL
ENV VITEAPIURL=$VITEAPIURL
ENV VITE_API_URL=$VITEAPIURL
ENV VITE_API_BASE_URL=$VITEAPIURL

RUN npm run build

FROM nginx:stable-alpine

COPY --from=builder /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
