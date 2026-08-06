# Project instructions

- Write test cases for all new code you introduce (new classes, new methods, new
  behavior in existing methods). Prefer extracting new logic into small, independently
  testable units when the natural location (e.g. `Siamese.insert()`) can't be tested
  directly — `SiameseTest` is `@Ignore`d because it requires a live Elasticsearch
  connection, so anything that needs to be verified without one should live in its own
  class under `crest.siamese.helpers` or similar, with its own test class.
