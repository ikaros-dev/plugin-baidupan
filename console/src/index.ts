import { definePlugin } from "@runikaros/shared"

export default definePlugin({
    name: 'PluginBaiDuPan',
    components: {},
    extensionPoints: {
        // @ts-ignore
        'file:remote': 'BaiDuPan'
    }
    
})