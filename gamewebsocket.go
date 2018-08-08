/* Adapt ws://...:6080 to attach to game socket on localhost:6081 */

package main

import (
	"log"
	"net"
	"net/http"
	"time"

	"golang.org/x/net/websocket"
)

func main() {
	http.Handle("/blasteroids", websocket.Server{Handler: wsh,
		Handshake: func(ws *websocket.Config, req *http.Request) error {
			ws.Protocol = []string{}
			return nil
		}})
	log.Fatal(http.ListenAndServe(":6080", nil))
}

func wsh(ws *websocket.Conn) {

	durationToAwaitServer, err := time.ParseDuration("60s")
	if err != nil {
		log.Println(err)
		return
	}

	loc := "127.0.0.1:6081"
	vc, err := net.Dial("tcp", loc)

	if err != nil {
		log.Println(err)
		return
	}

	go func() {
		defer vc.Close()
		defer ws.Close()
		buf := make([]byte, 32*1024)
		for {
			n, e := ws.Read(buf)
			if e != nil {
				break
			}
			n, e = vc.Write(buf[0:n])
			if e != nil {
				break
			}
		}
	}()

	go func() {
		defer vc.Close()
		defer ws.Close()
		buf := make([]byte, 32*1024)
		for {
			vc.SetReadDeadline(time.Now().Add(durationToAwaitServer))
			n, e := vc.Read(buf)
			if e != nil {
				break
			}
			n, e = ws.Write(buf[0:n])
			if e != nil {
				break
			}
		}
	}()

	select {}
}
