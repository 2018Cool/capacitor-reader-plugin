import { WebPlugin } from '@capacitor/core';
import { ReaderPluginPlugin } from './definitions';
export declare class ReaderPluginWeb extends WebPlugin implements ReaderPluginPlugin {
    constructor();
    openFile(options: {
        url: string;
        title: string;
        navbarColor: string;
    }): Promise<any>;
}
declare const ReaderPlugin: ReaderPluginWeb;
export { ReaderPlugin };
