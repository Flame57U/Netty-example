export default function (url, readerId, epcReadCallback, readerStatusCallback) {
    
    const webSocket = new ReconnectingWebSocket(url);

    webSocket.onopen = function (event) {
        console.log('rfid middleware websocket 连接成功。');
        if(readerStatusCallback != null){
            readerStatusCallback('已连接');
        }
        webSocket.send('Subscribe ' + readerId + '@All');
    };

    webSocket.onclose = function (event) {
        console.log('rfid middleware websocket 断开连接。');
        if(readerStatusCallback != null){
            readerStatusCallback('断开连接');
        }
    };

    webSocket.onerror = function (event) {
        console.log('rfid middleware websocket 出错。');
        if(readerStatusCallback != null){
            readerStatusCallback('连接错误');
        }
    };

    webSocket.onmessage = function (event) {
        const readEvents = JSON.parse(event.data);
        if (readEvents.code === undefined) {
            for (const key in readEvents) {
                const datas = readEvents[key];
                if (datas != null && datas.length != 0) {
                    datas.forEach(function (epc) {
                        if (epcReadCallback != null) {
                            epcReadCallback(epc);
                        }
                    });
                }
            }
        }
    };
    
};
