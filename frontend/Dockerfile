FROM node:20-alpine AS build
WORKDIR /app

# Docker build context will be root dir
COPY frontend/package*.json frontend/
WORKDIR /app/frontend
RUN npm install --legacy-peer-deps

COPY frontend .
COPY proto ../proto
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/frontend/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
