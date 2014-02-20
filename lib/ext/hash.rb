class Hash
  def except(*a)
    self.dup.tap do |copy|
      copy.keep_if{ |k,v| !a.include?(k) }
    end
  end

  def extract(*a)
    self.dup.tap do |copy|
      copy.keep_if{ |k,v| a.include?(k) }
    end
  end

  def deep_merge(other, &block)
    self.dup.deep_merge!(other)
  end

  def deep_merge!(other, &block)
    other.each_pair do |k,v|
      tv = self[k]
      if tv.is_a?(Hash) && v.is_a?(Hash)
        self[k] = tv.deep_merge(v, &block)
      elsif tv.is_a?(Array) && v.is_a?(Array)
        self[k] = tv | v
      else
        self[k] = block && tv ? block.call(k, tv, v) : v
      end
    end
    self
  end
  
  # def key_to_sym
  #   Hash[self.map{ |k, v| [k.to_sym, v] }]
  # end
end
