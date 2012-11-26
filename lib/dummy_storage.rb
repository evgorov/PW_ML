class StorageNotSet < Exception; end

class DummyStorage
  def namespace(*); raise StorageNotSet; end
  def get(*); raise StorageNotSet; end
  def del(*); raise StorageNotSet; end
  def zadd(*); raise StorageNotSet; end
  def sadd(*); raise StorageNotSet; end
  def srange(*); raise StorageNotSet; end
  def set(*); raise StorageNotSet; end
end
