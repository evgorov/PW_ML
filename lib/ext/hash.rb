class Hash
  def extract(*a)
    self.dup.tap do |copy|
      copy.keep_if{ |k,v| a.include?(k) }
    end
  end
end

