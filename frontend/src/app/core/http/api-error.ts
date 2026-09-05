export function apiErrorMessage(err: unknown, fallback: string): string {
  const http = err as {
    status?: number;
    message?: string;
    error?: { message?: string; error?: string } | string;
  };
  if (http?.status === 0) {
    return 'Le backend Spring n’est pas joignable (http://localhost:8080). Lancez mvnw.cmd spring-boot:run puis réessayez.';
  }
  if (http?.status === 404) {
    return 'API introuvable (404). Vérifiez que Spring tourne sur le port 8080 et redémarrez ng serve.';
  }
  if (typeof http?.error === 'string' && http.error.includes('Cannot POST')) {
    return 'Le proxy Angular n’envoie pas le POST vers Spring. Redémarrez ng serve (npm start).';
  }
  if (http?.error && typeof http.error === 'object' && http.error.message) {
    return http.error.message;
  }
  return fallback;
}
