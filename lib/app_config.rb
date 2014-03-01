require 'yaml'
require 'i18n/core_ext/hash'

module AppConfig
  extend self
  @_settings = {}
  def load!(filename, options = {})
    newsets = YAML::load_file(filename).deep_symbolize_keys
    if options[:env] && newsets[options[:env].to_sym]
      newsets = newsets[options[:env].to_sym] 
    else
      newsets = newsets[:development] 
    end
    deep_merge!(@_settings, newsets)
  end

  # Deep merging of hashes
  # deep_merge by Stefan Rusterholz, see http://www.ruby-forum.com/topic/142809
  def deep_merge!(target, data)
    merger = proc{|key, v1, v2|
      Hash === v1 && Hash === v2 ? v1.merge(v2, &merger) : v2 }
    target.merge! data, &merger
  end

  def method_missing(name, *args, &block)
    @_settings[name.to_sym] ||
    fail(NoMethodError, "unknown configuration root #{name}", caller)
  end
end