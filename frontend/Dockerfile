# Stage 1: Build
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

# Copy source and build
COPY . .
RUN npm run build -- --configuration production

# Stage 3: Build finalized (using Alpine for small footprint)
FROM nginx:stable-alpine

# Copy built assets
# Angular 19 "application" builder outputs to dist/[project-name]/browser
COPY --from=build /app/dist/shopp-mate-front/browser /usr/share/nginx/html

# nginx configuration for SPA and API proxying
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
