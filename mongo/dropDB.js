conn = new Mongo();
db = conn.getDB("aquarium");
db.dropDatabase();