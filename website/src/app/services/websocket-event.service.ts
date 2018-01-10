import {EventEmitter, Injectable, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {$WebSocket, WebSocketSendMode} from "angular2-websocket/angular2-websocket";

@Injectable()
export class WebsocketEventService{

  // WebSocket HB间隔
  pingIntervalId

  // WebSocket客户端
  websocket

  // 平台初始启动
  platformFirstLoad = true;

  constructor(public router:Router) {

    this.websocket = new $WebSocket("ws://localhost:8999/ws")

    let vm = this;

    this.websocket.onOpen(function () {
      console.log('Websocket created')
      // 广播事件
      //vm.eventService.broadcast('setConnectedStatus', true)

      // 广播平台初始启动事件
      if(vm.platformFirstLoad){
        vm.platformFirstLoad = false
        //vm.eventService.broadcast('platformStartup')
      }

      // 发送心跳
      this.pingIntervalId = setInterval(function () {
        vm.sendNewEvent({op: 'PING'})
      }, 10000)
    })

    this.websocket.onMessage(
      (event: MessageEvent)=> {

        let payload
        if (event.data) {
          payload = JSON.parse(event.data)
        }

        console.log('Receive << %o, %o', payload.op, payload)

        let op = payload.op
        let data = payload.data
        if (op === 'NOTE') {
          // 加载具体的Note信息
          //vm.eventService.broadcast('setNoteContent', data.note)
        } else if (op === 'NEW_NOTE') {
          vm.router.navigate(['/notebook/' + data.note.id])
        } else if (op === 'NOTES_INFO') {
          // 启动加载Notes
          //vm.eventService.broadcast('setNoteMenu', data.notes)
        } else if (op === 'LIST_NOTE_JOBS') {
          // TODO $rootScope.$emit('jobmanager:set-jobs', data.noteJobs)
          //$rootScope.$emit('jobmanager:set-jobs', data.noteJobs)
        } else if (op === 'LIST_UPDATE_NOTE_JOBS') {
          // TODO $rootScope.$emit('jobmanager:update-jobs', data.noteRunningJobs)
          //$rootScope.$emit('jobmanager:update-jobs', data.noteRunningJobs)
        } else {
          console.error(`unknown websocket op: ${op}`)
        }

      },
      {autoApply: false}
    )

    this.websocket.onError((event: MessageEvent)=> {
      //vm.eventService.broadcast('setConnectedStatus', false)
    })

    this.websocket.onClose((event: MessageEvent)=> {
      console.log('close message: ', event)
      if (this.pingIntervalId !== null) {
        clearInterval(this.pingIntervalId)
        this.pingIntervalId = null
      }
      //vm.eventService.broadcast('setConnectedStatus', false)
    })

    /*while (!this.isConnected()){
      console.log(">>>>>>>>>>>")
    }*/


  }

  sendNewEvent(data) {
    console.log('Send >> %o, %o', data.op, data)
    return this.websocket.send(data,WebSocketSendMode.Direct)
  }

  isConnected() {
    return (this.websocket.socket.readyState === this.websocket.readyStateConstants.OPEN)
  }
}
