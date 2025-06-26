from fastapi import FastAPI

app = FastAPI(docs_url=None, redoc_url=None, openapi_url=None)

def create_app():
  from routes.main import main
  app.include_router(main)
  from routes.api import api
  app.include_router(api)
  return app