import { defineConfig } from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

export default defineConfig({
  plugins: [
    scalaJSPlugin({
      projectID: "front",
    }),
  ],
  server: {
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
});
