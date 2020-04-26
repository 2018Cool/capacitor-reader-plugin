
  Pod::Spec.new do |s|
    s.name = 'CapacitorReaderPlugin'
    s.version = '1.0.1'
    s.summary = 'document file reader'
    s.license = 'MIT'
    s.homepage = '_'
    s.author = 'zwc'
    s.source = { :git => '_', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end