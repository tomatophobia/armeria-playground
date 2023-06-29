new function () {
  let ws = null;
  let connected = false;

  let serverUrl;
  let connectionStatus;
  let sendMessage;

  let connectButton;
  let disconnectButton;
  let sendButton;

  let open = function () {
    let url = serverUrl.val();
    ws = new WebSocket(url);
    ws.onopen = onOpen;
    ws.onclose = onClose;
    ws.onmessage = onMessage;
    ws.onerror = onError;

    connectionStatus.text('OPENING ...');
    serverUrl.attr('disabled', 'disabled');
    connectButton.hide();
    disconnectButton.show();
  }

  let close = function () {
    if (ws) {
      console.log('CLOSING ...');
      ws.close();
    }
    connected = false;
    connectionStatus.text('CLOSED');

    serverUrl.removeAttr('disabled');
    connectButton.show();
    disconnectButton.hide();
    sendMessage.attr('disabled', 'disabled');
    sendButton.attr('disabled', 'disabled');
  }

  let clearLog = function () {
    $('#messages').html('');
  }

  let onOpen = function () {
    console.log('OPENED: ' + serverUrl.val());
    connected = true;
    connectionStatus.text('OPENED');
    sendMessage.removeAttr('disabled');
    sendButton.removeAttr('disabled');
  };

  let onClose = function () {
    console.log('CLOSED: ' + serverUrl.val());
    ws = null;
  };

  let onMessage = function (event) {
    let data = event.data;
    addMessage(data);
  };

  let onError = function (event) {
    alert(event.data);
  }

  let addMessage = function (data, type) {
    let msg = $('<pre>').text(data);
    if (type === 'SENT') {
      msg.addClass('sent');
    }
    let messages = $('#messages');
    messages.append(msg);

    let msgBox = messages.get(0);
    while (msgBox.childNodes.length > 1000) {
      msgBox.removeChild(msgBox.firstChild);
    }
    msgBox.scrollTop = msgBox.scrollHeight;
  }

  WebSocketClient = {
    init: function () {
      serverUrl = $('#serverUrl');
      connectionStatus = $('#connectionStatus');
      sendMessage = $('#sendMessage');

      connectButton = $('#connectButton');
      disconnectButton = $('#disconnectButton');
      sendButton = $('#sendButton');

      connectButton.click(function (e) {
        close();
        open();
      });

      disconnectButton.click(function (e) {
        close();
      });

      sendButton.click(function (e) {
        let msg = $('#sendMessage').val();
        addMessage(msg, 'SENT');

        // binary 타입 전송 테스트
        // var buffer = new ArrayBuffer(8);
        // var dataview = new DataView(buffer);
        // dataview.setInt32(0, 9438);
        // dataview.setFloat32(4, 3224.3224);
        // ws.send(buffer);

        ws.send(msg);
      });

      $('#clearMessage').click(function (e) {
        clearLog();
      });

      let isCtrl;
      sendMessage.keyup(function (e) {
        if (e.which == 17) isCtrl = false;
      }).keydown(function (e) {
        if (e.which == 17) isCtrl = true;
        if (e.which == 13 && isCtrl == true) {
          sendButton.click();
          return false;
        }
      });
    }
  };
}

$(function () {
  WebSocketClient.init();
});