require 'yaml'
require 'i18n/core_ext/hash'
require 'ext/hash'

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
    @_settings.deep_merge!(newsets)
  end

  def method_missing(name, *args, &block)
    @_settings[name.to_sym] ||
    fail(NoMethodError, "unknown configuration root #{name}", caller)
  end
end
