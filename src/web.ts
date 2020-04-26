import { WebPlugin } from '@capacitor/core';
import { ReaderPluginPlugin } from './definitions';

export class ReaderPluginWeb extends WebPlugin implements ReaderPluginPlugin {
  constructor() {
    super({
      name: 'ReaderPlugin',
      platforms: ['web']
    });
  }

  async openFile(options: { url: string,title:string,navbarColor:string }): Promise<any> {
    console.log('ECHO', options);
    return options;
  }
}

const ReaderPlugin = new ReaderPluginWeb();

export { ReaderPlugin };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(ReaderPlugin);
