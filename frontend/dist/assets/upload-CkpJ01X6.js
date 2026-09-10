import{r as e}from"./index-DZoTTAyQ.js";const n=a=>a.data;async function p(a){const o=new FormData;for(const t of a)o.append("files",t);return e.post("/api/upload/image",o).then(n)}export{p as u};
