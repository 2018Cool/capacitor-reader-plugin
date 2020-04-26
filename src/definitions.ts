declare module "@capacitor/core" {
  interface PluginRegistry {
    ReaderPlugin: ReaderPluginPlugin;
  }
}

export interface ReaderPluginPlugin {
  openFile(options: { url: string,title:string,navbarColor:string }): Promise<any>;
}
