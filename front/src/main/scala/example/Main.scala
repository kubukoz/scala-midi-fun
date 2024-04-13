import cats.effect.unsafe.implicits.global
import org.scalajs.dom
import org.scalajs.dom.*
import org.soundsofscala.models.*
import org.soundsofscala.models.AtomicMusicalEvent.Note
import org.soundsofscala.syntax.all.*
import org.soundsofscala.transport.Sequencer
import org.soundsofscala.Instruments.*
import scalajs.js

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JSON

@js.native
trait MidiMessage extends js.Object {
  val data: js.Array[Int]
}

@js.native
trait MidiInput extends js.Object {
  var onmidimessage: js.Function1[MidiMessage, Unit]
}

@js.native
trait MidiAccess extends js.Any {
  var inputs: js.Map[String, MidiInput]
}

enum MidiEvent {
  def note: Int

  case NoteOn(channel: Int, note: Int, velocity: Int)
  case NoteOff(channel: Int, note: Int, velocity: Int)
}
def decodeMidi(data: Array[Int]): MidiEvent = {
  import MidiEvent.*
  val status   = data(0) & 0xf0
  val channel  = data(0) & 0x0f
  val note     = data(1)
  val velocity = data(2)
  status match {
    case 0x90 => NoteOn(channel, note, velocity)
    case 0x80 => NoteOff(channel, note, velocity)
  }

}

def notePretty(note: Int): String = {
  val notes  = List("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
  val octave = note / 12 - 1
  notes(note % 12) + octave
}

@main
def helloWorld(): Unit = {
  val navi = window.navigator
  window.addEventListener(
    "DOMContentLoaded",
    _ =>
      navi.permissions
        .query(new PermissionDescriptor {
          val name  = PermissionName.midi
          val sysex = true
        })
        .toFuture
        .flatMap { p =>
          navi.asInstanceOf[js.Dynamic].requestMIDIAccess().asInstanceOf[js.Promise[MidiAccess]].toFuture
        }
        .map { midiAccess =>
          midiAccess.inputs.foreach { (k, input) =>
            input.onmidimessage = (msg: MidiMessage) => {
              val decoded = decodeMidi(msg.data.toArray)
              println(
                s"MIDI message received: ${msg.data}, decoded: ${decoded} (note pretty: ${notePretty(decoded.note)})"
              )

              decoded match {
                case MidiEvent.NoteOn(channel, note, velocity) =>
                  val pitch: Pitch = Pitch.valueOf(
                    notePretty(note).take(1)
                  )
                  val accidental: Accidental =
                    if (notePretty(note).contains("#")) Accidental.Sharp else Accidental.Natural
                  val theSound: MusicalEvent = Note(
                    pitch = pitch,
                    accidental = accidental,
                    octave = Octave.from(note / 12).fold(e => throw new Exception(e), identity),
                    duration = Duration.ThirtySecond,
                    velocity = Velocity.Medium,
                  )

                  given AudioContext = AudioContext()

                  given ScalaSynth = new ScalaSynth()

                  val sequencer = Sequencer()

                  val song = Song(
                    title = Title("Test Song"),
                    tempo = Tempo(240),
                    swing = Swing.Straight,
                    mixer = Mixer(
                      Track(
                        title = Title("Test Track"),
                        musicalEvent = theSound,
                        instrument = given_ScalaSynth,
                      )
                    ),
                  )

                  sequencer
                    .playSong(
                      song
                    )
                    .unsafeRunAndForget()
                case _ => ()
              }

            }
          }

        },
  )
}
//   val quickTestSong =
//     C7 + G7 * 5 /* + F7 + G7 + G7.sharp + G7 +
//       RestHalf + RestWhole +
//       C7 + G7 * 5 + F7 + G7 + E7.flat + RestQuarter + C7 + RestQuarter + D7 + RestQuarter + B6.flat + RestQuarter */

//   dom.document.addEventListener(
//     "click",
//     _ => {
// //
//       val sounds = List(220, 493.88, 554.37, 587.33, 659.25, 739.99, 830.61, 880)
//       var i      = 0

//       given audioContext: AudioContext = new AudioContext()
//       val gain                         = audioContext.createGain()
//       gain.connect(audioContext.destination)

//       def next(): Unit = {

//         val osc = audioContext.createOscillator()
//         osc.`type` = "sawtooth"

//         osc.connect(gain)

//         gain.gain.value = 0.1

//         osc.frequency.value = sounds(i)
//         println("seemingly playing at frequency " + sounds(i) + " Hz")
//         osc.start(0)
//         osc.stop(1000.0 / sounds(i) / 5)

//         osc.onended = _ => {
//           osc.disconnect(gain)
//           i = (i + 1) % sounds.length
//           println("playing sound " + i)
//           next()
//         }
//       }
//       next()

//     },
//   )
