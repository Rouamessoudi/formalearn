FROM python:3.12-slim
WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1
COPY requirements-runtime.txt .
RUN pip install --no-cache-dir -r requirements-runtime.txt
COPY features.py serve.py ./
COPY models ./models
EXPOSE 8000
HEALTHCHECK --interval=20s --timeout=5s --retries=8 CMD python -c "import urllib.request; urllib.request.urlopen('http://127.0.0.1:8000/health')"
CMD ["python", "-m", "uvicorn", "serve:app", "--host", "0.0.0.0", "--port", "8000"]
