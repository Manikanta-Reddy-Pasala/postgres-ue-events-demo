/*eslint-disable block-scoped-var, id-length, no-control-regex, no-magic-numbers, no-mixed-operators, no-prototype-builtins, no-redeclare, no-shadow, no-var, sort-vars, default-case, jsdoc/require-param*/
"use strict";

var $protobuf = require("protobufjs/minimal");

// Common aliases
var $Reader = $protobuf.Reader, $Writer = $protobuf.Writer, $util = $protobuf.util;
var $Object = $util.global.Object, $undefined = $util.global.undefined, $Error = $util.global.Error, $TypeError = $util.global.TypeError, $String = $util.global.String, $Number = $util.global.Number, $Boolean = $util.global.Boolean, $Array = $util.global.Array, $parseInt = $util.global.parseInt, $BigInt = $util.global.BigInt;

// Exported root namespace
var $root = $protobuf.roots["default"] || ($protobuf.roots["default"] = {});

$root.com = (function() {

    /**
     * Namespace com.
     * @exports com
     * @namespace
     */
    var com = {};

    com.example = (function() {

        /**
         * Namespace example.
         * @memberof com
         * @namespace
         */
        var example = {};

        example.ue = (function() {

            /**
             * Namespace ue.
             * @memberof com.example
             * @namespace
             */
            var ue = {};

            /**
             * ActionTaken enum.
             * @name com.example.ue.ActionTaken
             * @enum {number}
             * @property {number} UNKNOWN_ACTION=0 UNKNOWN_ACTION value
             * @property {number} REJECT=1 REJECT value
             * @property {number} ATTACH=2 ATTACH value
             * @property {number} SILENT_CALL=3 SILENT_CALL value
             * @property {number} DETACH=4 DETACH value
             * @property {number} LOCATION_UPDATE=5 LOCATION_UPDATE value
             * @property {number} PAGING=6 PAGING value
             */
            ue.ActionTaken = (function() {
                var valuesById = {}, values = $Object.create(valuesById);
                values[valuesById[0] = "UNKNOWN_ACTION"] = 0;
                values[valuesById[1] = "REJECT"] = 1;
                values[valuesById[2] = "ATTACH"] = 2;
                values[valuesById[3] = "SILENT_CALL"] = 3;
                values[valuesById[4] = "DETACH"] = 4;
                values[valuesById[5] = "LOCATION_UPDATE"] = 5;
                values[valuesById[6] = "PAGING"] = 6;
                return values;
            })();

            /**
             * RatType enum.
             * @name com.example.ue.RatType
             * @enum {number}
             * @property {number} UNKNOWN_RAT=0 UNKNOWN_RAT value
             * @property {number} RAT_2G=1 RAT_2G value
             * @property {number} RAT_3G=2 RAT_3G value
             * @property {number} RAT_4G_LTE=3 RAT_4G_LTE value
             * @property {number} RAT_5G=4 RAT_5G value
             */
            ue.RatType = (function() {
                var valuesById = {}, values = $Object.create(valuesById);
                values[valuesById[0] = "UNKNOWN_RAT"] = 0;
                values[valuesById[1] = "RAT_2G"] = 1;
                values[valuesById[2] = "RAT_3G"] = 2;
                values[valuesById[3] = "RAT_4G_LTE"] = 3;
                values[valuesById[4] = "RAT_5G"] = 4;
                return values;
            })();

            ue.UeEvent = (function() {

                /**
                 * Properties of an UeEvent.
                 * @typedef {Object} com.example.ue.UeEvent.$Properties
                 * @property {string|null} [imsiOrSupi] UeEvent imsiOrSupi
                 * @property {string|null} [imei] UeEvent imei
                 * @property {string|null} [msisdn] UeEvent msisdn
                 * @property {string|null} [guti] UeEvent guti
                 * @property {string|null} [tmsi] UeEvent tmsi
                 * @property {number|null} [rssi] UeEvent rssi
                 * @property {com.example.ue.ActionTaken|null} [actionTaken] UeEvent actionTaken
                 * @property {number|null} [rejectCause] UeEvent rejectCause
                 * @property {com.example.ue.RatType|null} [rat] UeEvent rat
                 * @property {number|null} [frequencyBand] UeEvent frequencyBand
                 * @property {number|null} [arfcn] UeEvent arfcn
                 * @property {number|null} [trackingAreaCode] UeEvent trackingAreaCode
                 * @property {string|null} [downlinkBandWidth] UeEvent downlinkBandWidth
                 * @property {number|null} [plmnMcc] UeEvent plmnMcc
                 * @property {number|null} [plmnMnc] UeEvent plmnMnc
                 * @property {string|null} [providerName] UeEvent providerName
                 * @property {string|null} [missionId] UeEvent missionId
                 * @property {string|null} [sensorId] UeEvent sensorId
                 * @property {string|null} [subsystemId] UeEvent subsystemId
                 * @property {string|null} [trxCommandId] UeEvent trxCommandId
                 * @property {string|null} [createdAt] UeEvent createdAt
                 * @property {string|null} [updatedAt] UeEvent updatedAt
                 * @property {string|null} [countryIsoAlpha2] UeEvent countryIsoAlpha2
                 * @property {string|null} [countryName] UeEvent countryName
                 * @property {boolean|null} [target] UeEvent target
                 * @property {number|null} [captureCount] UeEvent captureCount
                 * @property {number|null} [timingAdvance] UeEvent timingAdvance
                 * @property {number|null} [distanceInMeters] UeEvent distanceInMeters
                 * @property {Array.<Uint8Array>} [$unknowns] Unknown fields preserved while decoding when enabled
                 */

                /**
                 * Properties of an UeEvent.
                 * @memberof com.example.ue
                 * @interface IUeEvent
                 * @augments com.example.ue.UeEvent.$Properties
                 * @deprecated Use com.example.ue.UeEvent.$Properties instead.
                 */

                /**
                 * Shape of an UeEvent.
                 * @typedef {com.example.ue.UeEvent.$Properties} com.example.ue.UeEvent.$Shape
                 */

                /**
                 * Constructs a new UeEvent.
                 * @memberof com.example.ue
                 * @classdesc Represents an UeEvent.
                 * @constructor
                 * @param {com.example.ue.UeEvent.$Properties=} [properties] Properties to set
                 * @property {Array.<Uint8Array>} [$unknowns] Unknown fields preserved while decoding when enabled
                 */
                var UeEvent = function (properties) {
                    if (properties)
                        for (var keys = $Object.keys(properties), i = 0; i < keys.length; ++i)
                            if (properties[keys[i]] != null && keys[i] !== "__proto__")
                                this[keys[i]] = properties[keys[i]];
                };

                /**
                 * UeEvent imsiOrSupi.
                 * @member {string} imsiOrSupi
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.imsiOrSupi = "";

                /**
                 * UeEvent imei.
                 * @member {string} imei
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.imei = "";

                /**
                 * UeEvent msisdn.
                 * @member {string} msisdn
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.msisdn = "";

                /**
                 * UeEvent guti.
                 * @member {string} guti
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.guti = "";

                /**
                 * UeEvent tmsi.
                 * @member {string} tmsi
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.tmsi = "";

                /**
                 * UeEvent rssi.
                 * @member {number} rssi
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.rssi = 0;

                /**
                 * UeEvent actionTaken.
                 * @member {com.example.ue.ActionTaken} actionTaken
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.actionTaken = 0;

                /**
                 * UeEvent rejectCause.
                 * @member {number} rejectCause
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.rejectCause = 0;

                /**
                 * UeEvent rat.
                 * @member {com.example.ue.RatType} rat
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.rat = 0;

                /**
                 * UeEvent frequencyBand.
                 * @member {number} frequencyBand
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.frequencyBand = 0;

                /**
                 * UeEvent arfcn.
                 * @member {number} arfcn
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.arfcn = 0;

                /**
                 * UeEvent trackingAreaCode.
                 * @member {number} trackingAreaCode
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.trackingAreaCode = 0;

                /**
                 * UeEvent downlinkBandWidth.
                 * @member {string} downlinkBandWidth
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.downlinkBandWidth = "";

                /**
                 * UeEvent plmnMcc.
                 * @member {number} plmnMcc
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.plmnMcc = 0;

                /**
                 * UeEvent plmnMnc.
                 * @member {number} plmnMnc
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.plmnMnc = 0;

                /**
                 * UeEvent providerName.
                 * @member {string} providerName
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.providerName = "";

                /**
                 * UeEvent missionId.
                 * @member {string} missionId
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.missionId = "";

                /**
                 * UeEvent sensorId.
                 * @member {string} sensorId
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.sensorId = "";

                /**
                 * UeEvent subsystemId.
                 * @member {string} subsystemId
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.subsystemId = "";

                /**
                 * UeEvent trxCommandId.
                 * @member {string} trxCommandId
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.trxCommandId = "";

                /**
                 * UeEvent createdAt.
                 * @member {string} createdAt
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.createdAt = "";

                /**
                 * UeEvent updatedAt.
                 * @member {string} updatedAt
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.updatedAt = "";

                /**
                 * UeEvent countryIsoAlpha2.
                 * @member {string} countryIsoAlpha2
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.countryIsoAlpha2 = "";

                /**
                 * UeEvent countryName.
                 * @member {string} countryName
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.countryName = "";

                /**
                 * UeEvent target.
                 * @member {boolean} target
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.target = false;

                /**
                 * UeEvent captureCount.
                 * @member {number} captureCount
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.captureCount = 0;

                /**
                 * UeEvent timingAdvance.
                 * @member {number} timingAdvance
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.timingAdvance = 0;

                /**
                 * UeEvent distanceInMeters.
                 * @member {number} distanceInMeters
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 */
                UeEvent.prototype.distanceInMeters = 0;

                /**
                 * Creates a new UeEvent instance using the specified properties.
                 * @function create
                 * @memberof com.example.ue.UeEvent
                 * @static
                 * @param {com.example.ue.UeEvent.$Properties=} [properties] Properties to set
                 * @returns {com.example.ue.UeEvent} UeEvent instance
                 * @type {{
                 *   (properties: com.example.ue.UeEvent.$Shape): com.example.ue.UeEvent & com.example.ue.UeEvent.$Shape;
                 *   (properties?: com.example.ue.UeEvent.$Properties): com.example.ue.UeEvent;
                 * }}
                 */
                UeEvent.create = function(properties) {
                    return new UeEvent(properties);
                };

                /**
                 * Encodes the specified UeEvent message. Does not implicitly {@link com.example.ue.UeEvent.verify|verify} messages.
                 * @function encode
                 * @memberof com.example.ue.UeEvent
                 * @static
                 * @param {com.example.ue.UeEvent.$Properties} message UeEvent message or plain object to encode
                 * @param {$protobuf.Writer} [writer] Writer to encode to
                 * @returns {$protobuf.Writer} Writer
                 */
                UeEvent.encode = function (message, writer, _depth) {
                    if (!writer)
                        writer = $Writer.create();
                    if (_depth === $undefined)
                        _depth = 0;
                    if (_depth > $util.recursionLimit)
                        throw $Error("max depth exceeded");
                    if (message.imsiOrSupi != null && $Object.hasOwnProperty.call(message, "imsiOrSupi"))
                        writer.uint32(/* id 1, wireType 2 =*/10).string(message.imsiOrSupi);
                    if (message.imei != null && $Object.hasOwnProperty.call(message, "imei"))
                        writer.uint32(/* id 2, wireType 2 =*/18).string(message.imei);
                    if (message.msisdn != null && $Object.hasOwnProperty.call(message, "msisdn"))
                        writer.uint32(/* id 3, wireType 2 =*/26).string(message.msisdn);
                    if (message.guti != null && $Object.hasOwnProperty.call(message, "guti"))
                        writer.uint32(/* id 4, wireType 2 =*/34).string(message.guti);
                    if (message.tmsi != null && $Object.hasOwnProperty.call(message, "tmsi"))
                        writer.uint32(/* id 5, wireType 2 =*/42).string(message.tmsi);
                    if (message.rssi != null && $Object.hasOwnProperty.call(message, "rssi"))
                        writer.uint32(/* id 6, wireType 0 =*/48).int32(message.rssi);
                    if (message.actionTaken != null && $Object.hasOwnProperty.call(message, "actionTaken"))
                        writer.uint32(/* id 7, wireType 0 =*/56).int32(message.actionTaken);
                    if (message.rejectCause != null && $Object.hasOwnProperty.call(message, "rejectCause"))
                        writer.uint32(/* id 8, wireType 0 =*/64).int32(message.rejectCause);
                    if (message.rat != null && $Object.hasOwnProperty.call(message, "rat"))
                        writer.uint32(/* id 9, wireType 0 =*/72).int32(message.rat);
                    if (message.frequencyBand != null && $Object.hasOwnProperty.call(message, "frequencyBand"))
                        writer.uint32(/* id 10, wireType 0 =*/80).int32(message.frequencyBand);
                    if (message.arfcn != null && $Object.hasOwnProperty.call(message, "arfcn"))
                        writer.uint32(/* id 11, wireType 0 =*/88).int32(message.arfcn);
                    if (message.trackingAreaCode != null && $Object.hasOwnProperty.call(message, "trackingAreaCode"))
                        writer.uint32(/* id 12, wireType 0 =*/96).int32(message.trackingAreaCode);
                    if (message.downlinkBandWidth != null && $Object.hasOwnProperty.call(message, "downlinkBandWidth"))
                        writer.uint32(/* id 13, wireType 2 =*/106).string(message.downlinkBandWidth);
                    if (message.plmnMcc != null && $Object.hasOwnProperty.call(message, "plmnMcc"))
                        writer.uint32(/* id 14, wireType 0 =*/112).int32(message.plmnMcc);
                    if (message.plmnMnc != null && $Object.hasOwnProperty.call(message, "plmnMnc"))
                        writer.uint32(/* id 15, wireType 0 =*/120).int32(message.plmnMnc);
                    if (message.providerName != null && $Object.hasOwnProperty.call(message, "providerName"))
                        writer.uint32(/* id 16, wireType 2 =*/130).string(message.providerName);
                    if (message.missionId != null && $Object.hasOwnProperty.call(message, "missionId"))
                        writer.uint32(/* id 17, wireType 2 =*/138).string(message.missionId);
                    if (message.sensorId != null && $Object.hasOwnProperty.call(message, "sensorId"))
                        writer.uint32(/* id 18, wireType 2 =*/146).string(message.sensorId);
                    if (message.subsystemId != null && $Object.hasOwnProperty.call(message, "subsystemId"))
                        writer.uint32(/* id 19, wireType 2 =*/154).string(message.subsystemId);
                    if (message.trxCommandId != null && $Object.hasOwnProperty.call(message, "trxCommandId"))
                        writer.uint32(/* id 20, wireType 2 =*/162).string(message.trxCommandId);
                    if (message.createdAt != null && $Object.hasOwnProperty.call(message, "createdAt"))
                        writer.uint32(/* id 21, wireType 2 =*/170).string(message.createdAt);
                    if (message.updatedAt != null && $Object.hasOwnProperty.call(message, "updatedAt"))
                        writer.uint32(/* id 22, wireType 2 =*/178).string(message.updatedAt);
                    if (message.countryIsoAlpha2 != null && $Object.hasOwnProperty.call(message, "countryIsoAlpha2"))
                        writer.uint32(/* id 23, wireType 2 =*/186).string(message.countryIsoAlpha2);
                    if (message.countryName != null && $Object.hasOwnProperty.call(message, "countryName"))
                        writer.uint32(/* id 24, wireType 2 =*/194).string(message.countryName);
                    if (message.target != null && $Object.hasOwnProperty.call(message, "target"))
                        writer.uint32(/* id 25, wireType 0 =*/200).bool(message.target);
                    if (message.captureCount != null && $Object.hasOwnProperty.call(message, "captureCount"))
                        writer.uint32(/* id 26, wireType 0 =*/208).int32(message.captureCount);
                    if (message.timingAdvance != null && $Object.hasOwnProperty.call(message, "timingAdvance"))
                        writer.uint32(/* id 27, wireType 0 =*/216).int32(message.timingAdvance);
                    if (message.distanceInMeters != null && $Object.hasOwnProperty.call(message, "distanceInMeters"))
                        writer.uint32(/* id 28, wireType 0 =*/224).int32(message.distanceInMeters);
                    if (message.$unknowns != null && $Object.hasOwnProperty.call(message, "$unknowns"))
                        for (var i = 0; i < message.$unknowns.length; ++i)
                            writer.raw(message.$unknowns[i]);
                    return writer;
                };

                /**
                 * Encodes the specified UeEvent message, length delimited. Does not implicitly {@link com.example.ue.UeEvent.verify|verify} messages.
                 * @function encodeDelimited
                 * @memberof com.example.ue.UeEvent
                 * @static
                 * @param {com.example.ue.UeEvent.$Properties} message UeEvent message or plain object to encode
                 * @param {$protobuf.Writer} [writer] Writer to encode to
                 * @returns {$protobuf.Writer} Writer
                 */
                UeEvent.encodeDelimited = function(message, writer) {
                    return this.encode(message, writer && writer.len ? writer.fork() : writer).ldelim();
                };

                /**
                 * Decodes an UeEvent message from the specified reader or buffer.
                 * @function decode
                 * @memberof com.example.ue.UeEvent
                 * @static
                 * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                 * @param {number} [length] Message length if known beforehand
                 * @returns {com.example.ue.UeEvent & com.example.ue.UeEvent.$Shape} UeEvent
                 * @throws {Error} If the payload is not a reader or valid buffer
                 * @throws {$protobuf.util.ProtocolError} If required fields are missing
                 */
                UeEvent.decode = function (reader, length, _end, _depth, _target) {
                    if (!(reader instanceof $Reader))
                        reader = $Reader.create(reader);
                    if (_depth === $undefined)
                        _depth = 0;
                    if (_depth > $Reader.recursionLimit)
                        throw $Error("max depth exceeded");
                    var end = length === $undefined ? reader.len : reader.pos + length, message = _target || new $root.com.example.ue.UeEvent(), value;
                    while (reader.pos < end) {
                        var start = reader.pos;
                        var tag = reader.tag();
                        if (tag === _end) {
                            _end = $undefined;
                            break;
                        }
                        var wireType = tag & 7;
                        switch (tag >>>= 3) {
                        case 1: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.imsiOrSupi = value;
                                else
                                    delete message.imsiOrSupi;
                                continue;
                            }
                        case 2: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.imei = value;
                                else
                                    delete message.imei;
                                continue;
                            }
                        case 3: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.msisdn = value;
                                else
                                    delete message.msisdn;
                                continue;
                            }
                        case 4: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.guti = value;
                                else
                                    delete message.guti;
                                continue;
                            }
                        case 5: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.tmsi = value;
                                else
                                    delete message.tmsi;
                                continue;
                            }
                        case 6: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.int32())
                                    message.rssi = value;
                                else
                                    delete message.rssi;
                                continue;
                            }
                        case 7: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.int32())
                                    message.actionTaken = value;
                                else
                                    delete message.actionTaken;
                                continue;
                            }
                        case 8: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.int32())
                                    message.rejectCause = value;
                                else
                                    delete message.rejectCause;
                                continue;
                            }
                        case 9: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.int32())
                                    message.rat = value;
                                else
                                    delete message.rat;
                                continue;
                            }
                        case 10: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.int32())
                                    message.frequencyBand = value;
                                else
                                    delete message.frequencyBand;
                                continue;
                            }
                        case 11: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.int32())
                                    message.arfcn = value;
                                else
                                    delete message.arfcn;
                                continue;
                            }
                        case 12: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.int32())
                                    message.trackingAreaCode = value;
                                else
                                    delete message.trackingAreaCode;
                                continue;
                            }
                        case 13: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.downlinkBandWidth = value;
                                else
                                    delete message.downlinkBandWidth;
                                continue;
                            }
                        case 14: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.int32())
                                    message.plmnMcc = value;
                                else
                                    delete message.plmnMcc;
                                continue;
                            }
                        case 15: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.int32())
                                    message.plmnMnc = value;
                                else
                                    delete message.plmnMnc;
                                continue;
                            }
                        case 16: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.providerName = value;
                                else
                                    delete message.providerName;
                                continue;
                            }
                        case 17: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.missionId = value;
                                else
                                    delete message.missionId;
                                continue;
                            }
                        case 18: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.sensorId = value;
                                else
                                    delete message.sensorId;
                                continue;
                            }
                        case 19: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.subsystemId = value;
                                else
                                    delete message.subsystemId;
                                continue;
                            }
                        case 20: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.trxCommandId = value;
                                else
                                    delete message.trxCommandId;
                                continue;
                            }
                        case 21: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.createdAt = value;
                                else
                                    delete message.createdAt;
                                continue;
                            }
                        case 22: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.updatedAt = value;
                                else
                                    delete message.updatedAt;
                                continue;
                            }
                        case 23: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.countryIsoAlpha2 = value;
                                else
                                    delete message.countryIsoAlpha2;
                                continue;
                            }
                        case 24: {
                                if (wireType !== 2)
                                    break;
                                if ((value = reader.stringVerify()).length)
                                    message.countryName = value;
                                else
                                    delete message.countryName;
                                continue;
                            }
                        case 25: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.bool())
                                    message.target = value;
                                else
                                    delete message.target;
                                continue;
                            }
                        case 26: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.int32())
                                    message.captureCount = value;
                                else
                                    delete message.captureCount;
                                continue;
                            }
                        case 27: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.int32())
                                    message.timingAdvance = value;
                                else
                                    delete message.timingAdvance;
                                continue;
                            }
                        case 28: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.int32())
                                    message.distanceInMeters = value;
                                else
                                    delete message.distanceInMeters;
                                continue;
                            }
                        }
                        reader.skipType(wireType, _depth, tag);
                        if (!reader.discardUnknown) {
                            $util.makeProp(message, "$unknowns", false);
                            (message.$unknowns || (message.$unknowns = [])).push(reader.raw(start, reader.pos));
                        }
                    }
                    if (_end !== $undefined)
                        throw $Error("missing end group");
                    return message;
                };

                /**
                 * Decodes an UeEvent message from the specified reader or buffer, length delimited.
                 * @function decodeDelimited
                 * @memberof com.example.ue.UeEvent
                 * @static
                 * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                 * @returns {com.example.ue.UeEvent & com.example.ue.UeEvent.$Shape} UeEvent
                 * @throws {Error} If the payload is not a reader or valid buffer
                 * @throws {$protobuf.util.ProtocolError} If required fields are missing
                 */
                UeEvent.decodeDelimited = function(reader) {
                    if (!(reader instanceof $Reader))
                        reader = new $Reader(reader);
                    return this.decode(reader, reader.uint32());
                };

                /**
                 * Verifies an UeEvent message.
                 * @function verify
                 * @memberof com.example.ue.UeEvent
                 * @static
                 * @param {Object.<string,*>} message Plain object to verify
                 * @returns {string|null} `null` if valid, otherwise the reason why it is not
                 */
                UeEvent.verify = function (message, _depth) {
                    if (typeof message !== "object" || message === null)
                        return "object expected";
                    if (_depth === $undefined)
                        _depth = 0;
                    if (_depth > $util.recursionLimit)
                        return "max depth exceeded";
                    if (message.imsiOrSupi != null && $Object.hasOwnProperty.call(message, "imsiOrSupi"))
                        if (!$util.isString(message.imsiOrSupi))
                            return "imsiOrSupi: string expected";
                    if (message.imei != null && $Object.hasOwnProperty.call(message, "imei"))
                        if (!$util.isString(message.imei))
                            return "imei: string expected";
                    if (message.msisdn != null && $Object.hasOwnProperty.call(message, "msisdn"))
                        if (!$util.isString(message.msisdn))
                            return "msisdn: string expected";
                    if (message.guti != null && $Object.hasOwnProperty.call(message, "guti"))
                        if (!$util.isString(message.guti))
                            return "guti: string expected";
                    if (message.tmsi != null && $Object.hasOwnProperty.call(message, "tmsi"))
                        if (!$util.isString(message.tmsi))
                            return "tmsi: string expected";
                    if (message.rssi != null && $Object.hasOwnProperty.call(message, "rssi"))
                        if (!$util.isInteger(message.rssi))
                            return "rssi: integer expected";
                    if (message.actionTaken != null && $Object.hasOwnProperty.call(message, "actionTaken"))
                        switch (message.actionTaken) {
                        default:
                            return "actionTaken: enum value expected";
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                            break;
                        }
                    if (message.rejectCause != null && $Object.hasOwnProperty.call(message, "rejectCause"))
                        if (!$util.isInteger(message.rejectCause))
                            return "rejectCause: integer expected";
                    if (message.rat != null && $Object.hasOwnProperty.call(message, "rat"))
                        switch (message.rat) {
                        default:
                            return "rat: enum value expected";
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            break;
                        }
                    if (message.frequencyBand != null && $Object.hasOwnProperty.call(message, "frequencyBand"))
                        if (!$util.isInteger(message.frequencyBand))
                            return "frequencyBand: integer expected";
                    if (message.arfcn != null && $Object.hasOwnProperty.call(message, "arfcn"))
                        if (!$util.isInteger(message.arfcn))
                            return "arfcn: integer expected";
                    if (message.trackingAreaCode != null && $Object.hasOwnProperty.call(message, "trackingAreaCode"))
                        if (!$util.isInteger(message.trackingAreaCode))
                            return "trackingAreaCode: integer expected";
                    if (message.downlinkBandWidth != null && $Object.hasOwnProperty.call(message, "downlinkBandWidth"))
                        if (!$util.isString(message.downlinkBandWidth))
                            return "downlinkBandWidth: string expected";
                    if (message.plmnMcc != null && $Object.hasOwnProperty.call(message, "plmnMcc"))
                        if (!$util.isInteger(message.plmnMcc))
                            return "plmnMcc: integer expected";
                    if (message.plmnMnc != null && $Object.hasOwnProperty.call(message, "plmnMnc"))
                        if (!$util.isInteger(message.plmnMnc))
                            return "plmnMnc: integer expected";
                    if (message.providerName != null && $Object.hasOwnProperty.call(message, "providerName"))
                        if (!$util.isString(message.providerName))
                            return "providerName: string expected";
                    if (message.missionId != null && $Object.hasOwnProperty.call(message, "missionId"))
                        if (!$util.isString(message.missionId))
                            return "missionId: string expected";
                    if (message.sensorId != null && $Object.hasOwnProperty.call(message, "sensorId"))
                        if (!$util.isString(message.sensorId))
                            return "sensorId: string expected";
                    if (message.subsystemId != null && $Object.hasOwnProperty.call(message, "subsystemId"))
                        if (!$util.isString(message.subsystemId))
                            return "subsystemId: string expected";
                    if (message.trxCommandId != null && $Object.hasOwnProperty.call(message, "trxCommandId"))
                        if (!$util.isString(message.trxCommandId))
                            return "trxCommandId: string expected";
                    if (message.createdAt != null && $Object.hasOwnProperty.call(message, "createdAt"))
                        if (!$util.isString(message.createdAt))
                            return "createdAt: string expected";
                    if (message.updatedAt != null && $Object.hasOwnProperty.call(message, "updatedAt"))
                        if (!$util.isString(message.updatedAt))
                            return "updatedAt: string expected";
                    if (message.countryIsoAlpha2 != null && $Object.hasOwnProperty.call(message, "countryIsoAlpha2"))
                        if (!$util.isString(message.countryIsoAlpha2))
                            return "countryIsoAlpha2: string expected";
                    if (message.countryName != null && $Object.hasOwnProperty.call(message, "countryName"))
                        if (!$util.isString(message.countryName))
                            return "countryName: string expected";
                    if (message.target != null && $Object.hasOwnProperty.call(message, "target"))
                        if (typeof message.target !== "boolean")
                            return "target: boolean expected";
                    if (message.captureCount != null && $Object.hasOwnProperty.call(message, "captureCount"))
                        if (!$util.isInteger(message.captureCount))
                            return "captureCount: integer expected";
                    if (message.timingAdvance != null && $Object.hasOwnProperty.call(message, "timingAdvance"))
                        if (!$util.isInteger(message.timingAdvance))
                            return "timingAdvance: integer expected";
                    if (message.distanceInMeters != null && $Object.hasOwnProperty.call(message, "distanceInMeters"))
                        if (!$util.isInteger(message.distanceInMeters))
                            return "distanceInMeters: integer expected";
                    return null;
                };

                /**
                 * Creates an UeEvent message from a plain object. Also converts values to their respective internal types.
                 * @function fromObject
                 * @memberof com.example.ue.UeEvent
                 * @static
                 * @param {Object.<string,*>} object Plain object
                 * @returns {com.example.ue.UeEvent} UeEvent
                 */
                UeEvent.fromObject = function (object, _depth) {
                    if (object instanceof $root.com.example.ue.UeEvent)
                        return object;
                    if (!$util.isObject(object))
                        throw $TypeError(".com.example.ue.UeEvent: object expected");
                    if (_depth === $undefined)
                        _depth = 0;
                    if (_depth > $util.recursionLimit)
                        throw $Error("max depth exceeded");
                    var message = new $root.com.example.ue.UeEvent();
                    if (object.imsiOrSupi != null)
                        if (typeof object.imsiOrSupi !== "string" || object.imsiOrSupi.length)
                            message.imsiOrSupi = $String(object.imsiOrSupi);
                    if (object.imei != null)
                        if (typeof object.imei !== "string" || object.imei.length)
                            message.imei = $String(object.imei);
                    if (object.msisdn != null)
                        if (typeof object.msisdn !== "string" || object.msisdn.length)
                            message.msisdn = $String(object.msisdn);
                    if (object.guti != null)
                        if (typeof object.guti !== "string" || object.guti.length)
                            message.guti = $String(object.guti);
                    if (object.tmsi != null)
                        if (typeof object.tmsi !== "string" || object.tmsi.length)
                            message.tmsi = $String(object.tmsi);
                    if (object.rssi != null)
                        if ($Number(object.rssi) !== 0)
                            message.rssi = object.rssi | 0;
                    if (object.actionTaken !== 0 && (typeof object.actionTaken !== "string" || $root.com.example.ue.ActionTaken[object.actionTaken] !== 0))
                        switch (object.actionTaken) {
                        default:
                            if (typeof object.actionTaken === "number") {
                                message.actionTaken = object.actionTaken;
                                break;
                            }
                            break;
                        case "UNKNOWN_ACTION":
                        case 0:
                            message.actionTaken = 0;
                            break;
                        case "REJECT":
                        case 1:
                            message.actionTaken = 1;
                            break;
                        case "ATTACH":
                        case 2:
                            message.actionTaken = 2;
                            break;
                        case "SILENT_CALL":
                        case 3:
                            message.actionTaken = 3;
                            break;
                        case "DETACH":
                        case 4:
                            message.actionTaken = 4;
                            break;
                        case "LOCATION_UPDATE":
                        case 5:
                            message.actionTaken = 5;
                            break;
                        case "PAGING":
                        case 6:
                            message.actionTaken = 6;
                            break;
                        }
                    if (object.rejectCause != null)
                        if ($Number(object.rejectCause) !== 0)
                            message.rejectCause = object.rejectCause | 0;
                    if (object.rat !== 0 && (typeof object.rat !== "string" || $root.com.example.ue.RatType[object.rat] !== 0))
                        switch (object.rat) {
                        default:
                            if (typeof object.rat === "number") {
                                message.rat = object.rat;
                                break;
                            }
                            break;
                        case "UNKNOWN_RAT":
                        case 0:
                            message.rat = 0;
                            break;
                        case "RAT_2G":
                        case 1:
                            message.rat = 1;
                            break;
                        case "RAT_3G":
                        case 2:
                            message.rat = 2;
                            break;
                        case "RAT_4G_LTE":
                        case 3:
                            message.rat = 3;
                            break;
                        case "RAT_5G":
                        case 4:
                            message.rat = 4;
                            break;
                        }
                    if (object.frequencyBand != null)
                        if ($Number(object.frequencyBand) !== 0)
                            message.frequencyBand = object.frequencyBand | 0;
                    if (object.arfcn != null)
                        if ($Number(object.arfcn) !== 0)
                            message.arfcn = object.arfcn | 0;
                    if (object.trackingAreaCode != null)
                        if ($Number(object.trackingAreaCode) !== 0)
                            message.trackingAreaCode = object.trackingAreaCode | 0;
                    if (object.downlinkBandWidth != null)
                        if (typeof object.downlinkBandWidth !== "string" || object.downlinkBandWidth.length)
                            message.downlinkBandWidth = $String(object.downlinkBandWidth);
                    if (object.plmnMcc != null)
                        if ($Number(object.plmnMcc) !== 0)
                            message.plmnMcc = object.plmnMcc | 0;
                    if (object.plmnMnc != null)
                        if ($Number(object.plmnMnc) !== 0)
                            message.plmnMnc = object.plmnMnc | 0;
                    if (object.providerName != null)
                        if (typeof object.providerName !== "string" || object.providerName.length)
                            message.providerName = $String(object.providerName);
                    if (object.missionId != null)
                        if (typeof object.missionId !== "string" || object.missionId.length)
                            message.missionId = $String(object.missionId);
                    if (object.sensorId != null)
                        if (typeof object.sensorId !== "string" || object.sensorId.length)
                            message.sensorId = $String(object.sensorId);
                    if (object.subsystemId != null)
                        if (typeof object.subsystemId !== "string" || object.subsystemId.length)
                            message.subsystemId = $String(object.subsystemId);
                    if (object.trxCommandId != null)
                        if (typeof object.trxCommandId !== "string" || object.trxCommandId.length)
                            message.trxCommandId = $String(object.trxCommandId);
                    if (object.createdAt != null)
                        if (typeof object.createdAt !== "string" || object.createdAt.length)
                            message.createdAt = $String(object.createdAt);
                    if (object.updatedAt != null)
                        if (typeof object.updatedAt !== "string" || object.updatedAt.length)
                            message.updatedAt = $String(object.updatedAt);
                    if (object.countryIsoAlpha2 != null)
                        if (typeof object.countryIsoAlpha2 !== "string" || object.countryIsoAlpha2.length)
                            message.countryIsoAlpha2 = $String(object.countryIsoAlpha2);
                    if (object.countryName != null)
                        if (typeof object.countryName !== "string" || object.countryName.length)
                            message.countryName = $String(object.countryName);
                    if (object.target != null)
                        if (object.target)
                            message.target = $Boolean(object.target);
                    if (object.captureCount != null)
                        if ($Number(object.captureCount) !== 0)
                            message.captureCount = object.captureCount | 0;
                    if (object.timingAdvance != null)
                        if ($Number(object.timingAdvance) !== 0)
                            message.timingAdvance = object.timingAdvance | 0;
                    if (object.distanceInMeters != null)
                        if ($Number(object.distanceInMeters) !== 0)
                            message.distanceInMeters = object.distanceInMeters | 0;
                    return message;
                };

                /**
                 * Creates a plain object from an UeEvent message. Also converts values to other types if specified.
                 * @function toObject
                 * @memberof com.example.ue.UeEvent
                 * @static
                 * @param {com.example.ue.UeEvent} message UeEvent
                 * @param {$protobuf.IConversionOptions} [options] Conversion options
                 * @returns {Object.<string,*>} Plain object
                 */
                UeEvent.toObject = function (message, options, _depth) {
                    if (!options)
                        options = {};
                    if (_depth === $undefined)
                        _depth = 0;
                    if (_depth > $util.recursionLimit)
                        throw $Error("max depth exceeded");
                    var object = {};
                    if (options.defaults) {
                        object.imsiOrSupi = "";
                        object.imei = "";
                        object.msisdn = "";
                        object.guti = "";
                        object.tmsi = "";
                        object.rssi = 0;
                        object.actionTaken = options.enums === $String ? "UNKNOWN_ACTION" : 0;
                        object.rejectCause = 0;
                        object.rat = options.enums === $String ? "UNKNOWN_RAT" : 0;
                        object.frequencyBand = 0;
                        object.arfcn = 0;
                        object.trackingAreaCode = 0;
                        object.downlinkBandWidth = "";
                        object.plmnMcc = 0;
                        object.plmnMnc = 0;
                        object.providerName = "";
                        object.missionId = "";
                        object.sensorId = "";
                        object.subsystemId = "";
                        object.trxCommandId = "";
                        object.createdAt = "";
                        object.updatedAt = "";
                        object.countryIsoAlpha2 = "";
                        object.countryName = "";
                        object.target = false;
                        object.captureCount = 0;
                        object.timingAdvance = 0;
                        object.distanceInMeters = 0;
                    }
                    if (message.imsiOrSupi != null && $Object.hasOwnProperty.call(message, "imsiOrSupi"))
                        object.imsiOrSupi = message.imsiOrSupi;
                    if (message.imei != null && $Object.hasOwnProperty.call(message, "imei"))
                        object.imei = message.imei;
                    if (message.msisdn != null && $Object.hasOwnProperty.call(message, "msisdn"))
                        object.msisdn = message.msisdn;
                    if (message.guti != null && $Object.hasOwnProperty.call(message, "guti"))
                        object.guti = message.guti;
                    if (message.tmsi != null && $Object.hasOwnProperty.call(message, "tmsi"))
                        object.tmsi = message.tmsi;
                    if (message.rssi != null && $Object.hasOwnProperty.call(message, "rssi"))
                        object.rssi = message.rssi;
                    if (message.actionTaken != null && $Object.hasOwnProperty.call(message, "actionTaken"))
                        object.actionTaken = options.enums === $String ? $root.com.example.ue.ActionTaken[message.actionTaken] === $undefined ? message.actionTaken : $root.com.example.ue.ActionTaken[message.actionTaken] : message.actionTaken;
                    if (message.rejectCause != null && $Object.hasOwnProperty.call(message, "rejectCause"))
                        object.rejectCause = message.rejectCause;
                    if (message.rat != null && $Object.hasOwnProperty.call(message, "rat"))
                        object.rat = options.enums === $String ? $root.com.example.ue.RatType[message.rat] === $undefined ? message.rat : $root.com.example.ue.RatType[message.rat] : message.rat;
                    if (message.frequencyBand != null && $Object.hasOwnProperty.call(message, "frequencyBand"))
                        object.frequencyBand = message.frequencyBand;
                    if (message.arfcn != null && $Object.hasOwnProperty.call(message, "arfcn"))
                        object.arfcn = message.arfcn;
                    if (message.trackingAreaCode != null && $Object.hasOwnProperty.call(message, "trackingAreaCode"))
                        object.trackingAreaCode = message.trackingAreaCode;
                    if (message.downlinkBandWidth != null && $Object.hasOwnProperty.call(message, "downlinkBandWidth"))
                        object.downlinkBandWidth = message.downlinkBandWidth;
                    if (message.plmnMcc != null && $Object.hasOwnProperty.call(message, "plmnMcc"))
                        object.plmnMcc = message.plmnMcc;
                    if (message.plmnMnc != null && $Object.hasOwnProperty.call(message, "plmnMnc"))
                        object.plmnMnc = message.plmnMnc;
                    if (message.providerName != null && $Object.hasOwnProperty.call(message, "providerName"))
                        object.providerName = message.providerName;
                    if (message.missionId != null && $Object.hasOwnProperty.call(message, "missionId"))
                        object.missionId = message.missionId;
                    if (message.sensorId != null && $Object.hasOwnProperty.call(message, "sensorId"))
                        object.sensorId = message.sensorId;
                    if (message.subsystemId != null && $Object.hasOwnProperty.call(message, "subsystemId"))
                        object.subsystemId = message.subsystemId;
                    if (message.trxCommandId != null && $Object.hasOwnProperty.call(message, "trxCommandId"))
                        object.trxCommandId = message.trxCommandId;
                    if (message.createdAt != null && $Object.hasOwnProperty.call(message, "createdAt"))
                        object.createdAt = message.createdAt;
                    if (message.updatedAt != null && $Object.hasOwnProperty.call(message, "updatedAt"))
                        object.updatedAt = message.updatedAt;
                    if (message.countryIsoAlpha2 != null && $Object.hasOwnProperty.call(message, "countryIsoAlpha2"))
                        object.countryIsoAlpha2 = message.countryIsoAlpha2;
                    if (message.countryName != null && $Object.hasOwnProperty.call(message, "countryName"))
                        object.countryName = message.countryName;
                    if (message.target != null && $Object.hasOwnProperty.call(message, "target"))
                        object.target = message.target;
                    if (message.captureCount != null && $Object.hasOwnProperty.call(message, "captureCount"))
                        object.captureCount = message.captureCount;
                    if (message.timingAdvance != null && $Object.hasOwnProperty.call(message, "timingAdvance"))
                        object.timingAdvance = message.timingAdvance;
                    if (message.distanceInMeters != null && $Object.hasOwnProperty.call(message, "distanceInMeters"))
                        object.distanceInMeters = message.distanceInMeters;
                    return object;
                };

                /**
                 * Converts this UeEvent to JSON.
                 * @function toJSON
                 * @memberof com.example.ue.UeEvent
                 * @instance
                 * @returns {Object.<string,*>} JSON object
                 */
                UeEvent.prototype.toJSON = function() {
                    return UeEvent.toObject(this, $protobuf.util.toJSONOptions);
                };

                /**
                 * Gets the type url for UeEvent
                 * @function getTypeUrl
                 * @memberof com.example.ue.UeEvent
                 * @static
                 * @param {string} [prefix] Custom type url prefix, defaults to `"type.googleapis.com"`
                 * @returns {string} The type url
                 */
                UeEvent.getTypeUrl = function(prefix) {
                    if (prefix === $undefined)
                        prefix = "type.googleapis.com";
                    return prefix + "/com.example.ue.UeEvent";
                };

                return UeEvent;
            })();

            ue.UeEventPageResponse = (function() {

                /**
                 * Properties of an UeEventPageResponse.
                 * @typedef {Object} com.example.ue.UeEventPageResponse.$Properties
                 * @property {Array.<com.example.ue.UeEvent.$Properties>|null} [events] UeEventPageResponse events
                 * @property {number|null} [totalPages] UeEventPageResponse totalPages
                 * @property {number|Long|null} [totalElements] UeEventPageResponse totalElements
                 * @property {number|null} [currentPage] UeEventPageResponse currentPage
                 * @property {number|Long|null} [queryTimeMs] UeEventPageResponse queryTimeMs
                 * @property {Array.<Uint8Array>} [$unknowns] Unknown fields preserved while decoding when enabled
                 */

                /**
                 * Properties of an UeEventPageResponse.
                 * @memberof com.example.ue
                 * @interface IUeEventPageResponse
                 * @augments com.example.ue.UeEventPageResponse.$Properties
                 * @deprecated Use com.example.ue.UeEventPageResponse.$Properties instead.
                 */

                /**
                 * Shape of an UeEventPageResponse.
                 * @typedef {com.example.ue.UeEventPageResponse.$Properties} com.example.ue.UeEventPageResponse.$Shape
                 */

                /**
                 * Constructs a new UeEventPageResponse.
                 * @memberof com.example.ue
                 * @classdesc Represents an UeEventPageResponse.
                 * @constructor
                 * @param {com.example.ue.UeEventPageResponse.$Properties=} [properties] Properties to set
                 * @property {Array.<Uint8Array>} [$unknowns] Unknown fields preserved while decoding when enabled
                 */
                var UeEventPageResponse = function (properties) {
                    this.events = [];
                    if (properties)
                        for (var keys = $Object.keys(properties), i = 0; i < keys.length; ++i)
                            if (properties[keys[i]] != null && keys[i] !== "__proto__")
                                this[keys[i]] = properties[keys[i]];
                };

                /**
                 * UeEventPageResponse events.
                 * @member {Array.<com.example.ue.UeEvent.$Properties>} events
                 * @memberof com.example.ue.UeEventPageResponse
                 * @instance
                 */
                UeEventPageResponse.prototype.events = $util.emptyArray;

                /**
                 * UeEventPageResponse totalPages.
                 * @member {number} totalPages
                 * @memberof com.example.ue.UeEventPageResponse
                 * @instance
                 */
                UeEventPageResponse.prototype.totalPages = 0;

                /**
                 * UeEventPageResponse totalElements.
                 * @member {number|Long} totalElements
                 * @memberof com.example.ue.UeEventPageResponse
                 * @instance
                 */
                UeEventPageResponse.prototype.totalElements = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

                /**
                 * UeEventPageResponse currentPage.
                 * @member {number} currentPage
                 * @memberof com.example.ue.UeEventPageResponse
                 * @instance
                 */
                UeEventPageResponse.prototype.currentPage = 0;

                /**
                 * UeEventPageResponse queryTimeMs.
                 * @member {number|Long} queryTimeMs
                 * @memberof com.example.ue.UeEventPageResponse
                 * @instance
                 */
                UeEventPageResponse.prototype.queryTimeMs = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

                /**
                 * Creates a new UeEventPageResponse instance using the specified properties.
                 * @function create
                 * @memberof com.example.ue.UeEventPageResponse
                 * @static
                 * @param {com.example.ue.UeEventPageResponse.$Properties=} [properties] Properties to set
                 * @returns {com.example.ue.UeEventPageResponse} UeEventPageResponse instance
                 * @type {{
                 *   (properties: com.example.ue.UeEventPageResponse.$Shape): com.example.ue.UeEventPageResponse & com.example.ue.UeEventPageResponse.$Shape;
                 *   (properties?: com.example.ue.UeEventPageResponse.$Properties): com.example.ue.UeEventPageResponse;
                 * }}
                 */
                UeEventPageResponse.create = function(properties) {
                    return new UeEventPageResponse(properties);
                };

                /**
                 * Encodes the specified UeEventPageResponse message. Does not implicitly {@link com.example.ue.UeEventPageResponse.verify|verify} messages.
                 * @function encode
                 * @memberof com.example.ue.UeEventPageResponse
                 * @static
                 * @param {com.example.ue.UeEventPageResponse.$Properties} message UeEventPageResponse message or plain object to encode
                 * @param {$protobuf.Writer} [writer] Writer to encode to
                 * @returns {$protobuf.Writer} Writer
                 */
                UeEventPageResponse.encode = function (message, writer, _depth) {
                    if (!writer)
                        writer = $Writer.create();
                    if (_depth === $undefined)
                        _depth = 0;
                    if (_depth > $util.recursionLimit)
                        throw $Error("max depth exceeded");
                    if (message.events != null && message.events.length)
                        for (var i = 0; i < message.events.length; ++i)
                            $root.com.example.ue.UeEvent.encode(message.events[i], writer.uint32(/* id 1, wireType 2 =*/10).fork(), _depth + 1).ldelim();
                    if (message.totalPages != null && $Object.hasOwnProperty.call(message, "totalPages"))
                        writer.uint32(/* id 2, wireType 0 =*/16).int32(message.totalPages);
                    if (message.totalElements != null && $Object.hasOwnProperty.call(message, "totalElements"))
                        writer.uint32(/* id 3, wireType 0 =*/24).int64(message.totalElements);
                    if (message.currentPage != null && $Object.hasOwnProperty.call(message, "currentPage"))
                        writer.uint32(/* id 4, wireType 0 =*/32).int32(message.currentPage);
                    if (message.queryTimeMs != null && $Object.hasOwnProperty.call(message, "queryTimeMs"))
                        writer.uint32(/* id 5, wireType 0 =*/40).int64(message.queryTimeMs);
                    if (message.$unknowns != null && $Object.hasOwnProperty.call(message, "$unknowns"))
                        for (var i = 0; i < message.$unknowns.length; ++i)
                            writer.raw(message.$unknowns[i]);
                    return writer;
                };

                /**
                 * Encodes the specified UeEventPageResponse message, length delimited. Does not implicitly {@link com.example.ue.UeEventPageResponse.verify|verify} messages.
                 * @function encodeDelimited
                 * @memberof com.example.ue.UeEventPageResponse
                 * @static
                 * @param {com.example.ue.UeEventPageResponse.$Properties} message UeEventPageResponse message or plain object to encode
                 * @param {$protobuf.Writer} [writer] Writer to encode to
                 * @returns {$protobuf.Writer} Writer
                 */
                UeEventPageResponse.encodeDelimited = function(message, writer) {
                    return this.encode(message, writer && writer.len ? writer.fork() : writer).ldelim();
                };

                /**
                 * Decodes an UeEventPageResponse message from the specified reader or buffer.
                 * @function decode
                 * @memberof com.example.ue.UeEventPageResponse
                 * @static
                 * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                 * @param {number} [length] Message length if known beforehand
                 * @returns {com.example.ue.UeEventPageResponse & com.example.ue.UeEventPageResponse.$Shape} UeEventPageResponse
                 * @throws {Error} If the payload is not a reader or valid buffer
                 * @throws {$protobuf.util.ProtocolError} If required fields are missing
                 */
                UeEventPageResponse.decode = function (reader, length, _end, _depth, _target) {
                    if (!(reader instanceof $Reader))
                        reader = $Reader.create(reader);
                    if (_depth === $undefined)
                        _depth = 0;
                    if (_depth > $Reader.recursionLimit)
                        throw $Error("max depth exceeded");
                    var end = length === $undefined ? reader.len : reader.pos + length, message = _target || new $root.com.example.ue.UeEventPageResponse(), value;
                    while (reader.pos < end) {
                        var start = reader.pos;
                        var tag = reader.tag();
                        if (tag === _end) {
                            _end = $undefined;
                            break;
                        }
                        var wireType = tag & 7;
                        switch (tag >>>= 3) {
                        case 1: {
                                if (wireType !== 2)
                                    break;
                                if (!(message.events && message.events.length))
                                    message.events = [];
                                message.events.push($root.com.example.ue.UeEvent.decode(reader, reader.uint32(), $undefined, _depth + 1));
                                continue;
                            }
                        case 2: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.int32())
                                    message.totalPages = value;
                                else
                                    delete message.totalPages;
                                continue;
                            }
                        case 3: {
                                if (wireType !== 0)
                                    break;
                                if (typeof (value = reader.int64()) === "object" ? value.low || value.high : value !== 0)
                                    message.totalElements = value;
                                else
                                    delete message.totalElements;
                                continue;
                            }
                        case 4: {
                                if (wireType !== 0)
                                    break;
                                if (value = reader.int32())
                                    message.currentPage = value;
                                else
                                    delete message.currentPage;
                                continue;
                            }
                        case 5: {
                                if (wireType !== 0)
                                    break;
                                if (typeof (value = reader.int64()) === "object" ? value.low || value.high : value !== 0)
                                    message.queryTimeMs = value;
                                else
                                    delete message.queryTimeMs;
                                continue;
                            }
                        }
                        reader.skipType(wireType, _depth, tag);
                        if (!reader.discardUnknown) {
                            $util.makeProp(message, "$unknowns", false);
                            (message.$unknowns || (message.$unknowns = [])).push(reader.raw(start, reader.pos));
                        }
                    }
                    if (_end !== $undefined)
                        throw $Error("missing end group");
                    return message;
                };

                /**
                 * Decodes an UeEventPageResponse message from the specified reader or buffer, length delimited.
                 * @function decodeDelimited
                 * @memberof com.example.ue.UeEventPageResponse
                 * @static
                 * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                 * @returns {com.example.ue.UeEventPageResponse & com.example.ue.UeEventPageResponse.$Shape} UeEventPageResponse
                 * @throws {Error} If the payload is not a reader or valid buffer
                 * @throws {$protobuf.util.ProtocolError} If required fields are missing
                 */
                UeEventPageResponse.decodeDelimited = function(reader) {
                    if (!(reader instanceof $Reader))
                        reader = new $Reader(reader);
                    return this.decode(reader, reader.uint32());
                };

                /**
                 * Verifies an UeEventPageResponse message.
                 * @function verify
                 * @memberof com.example.ue.UeEventPageResponse
                 * @static
                 * @param {Object.<string,*>} message Plain object to verify
                 * @returns {string|null} `null` if valid, otherwise the reason why it is not
                 */
                UeEventPageResponse.verify = function (message, _depth) {
                    if (typeof message !== "object" || message === null)
                        return "object expected";
                    if (_depth === $undefined)
                        _depth = 0;
                    if (_depth > $util.recursionLimit)
                        return "max depth exceeded";
                    if (message.events != null && $Object.hasOwnProperty.call(message, "events")) {
                        if (!$Array.isArray(message.events))
                            return "events: array expected";
                        for (var i = 0; i < message.events.length; ++i) {
                            var error = $root.com.example.ue.UeEvent.verify(message.events[i], _depth + 1);
                            if (error)
                                return "events." + error;
                        }
                    }
                    if (message.totalPages != null && $Object.hasOwnProperty.call(message, "totalPages"))
                        if (!$util.isInteger(message.totalPages))
                            return "totalPages: integer expected";
                    if (message.totalElements != null && $Object.hasOwnProperty.call(message, "totalElements"))
                        if (!$util.isInteger(message.totalElements) && !(message.totalElements && $util.isInteger(message.totalElements.low) && $util.isInteger(message.totalElements.high)))
                            return "totalElements: integer|Long expected";
                    if (message.currentPage != null && $Object.hasOwnProperty.call(message, "currentPage"))
                        if (!$util.isInteger(message.currentPage))
                            return "currentPage: integer expected";
                    if (message.queryTimeMs != null && $Object.hasOwnProperty.call(message, "queryTimeMs"))
                        if (!$util.isInteger(message.queryTimeMs) && !(message.queryTimeMs && $util.isInteger(message.queryTimeMs.low) && $util.isInteger(message.queryTimeMs.high)))
                            return "queryTimeMs: integer|Long expected";
                    return null;
                };

                /**
                 * Creates an UeEventPageResponse message from a plain object. Also converts values to their respective internal types.
                 * @function fromObject
                 * @memberof com.example.ue.UeEventPageResponse
                 * @static
                 * @param {Object.<string,*>} object Plain object
                 * @returns {com.example.ue.UeEventPageResponse} UeEventPageResponse
                 */
                UeEventPageResponse.fromObject = function (object, _depth) {
                    if (object instanceof $root.com.example.ue.UeEventPageResponse)
                        return object;
                    if (!$util.isObject(object))
                        throw $TypeError(".com.example.ue.UeEventPageResponse: object expected");
                    if (_depth === $undefined)
                        _depth = 0;
                    if (_depth > $util.recursionLimit)
                        throw $Error("max depth exceeded");
                    var message = new $root.com.example.ue.UeEventPageResponse();
                    if (object.events) {
                        if (!$Array.isArray(object.events))
                            throw $TypeError(".com.example.ue.UeEventPageResponse.events: array expected");
                        message.events = $Array(object.events.length);
                        for (var i = 0; i < object.events.length; ++i) {
                            if (!$util.isObject(object.events[i]))
                                throw $TypeError(".com.example.ue.UeEventPageResponse.events: object expected");
                            message.events[i] = $root.com.example.ue.UeEvent.fromObject(object.events[i], _depth + 1);
                        }
                    }
                    if (object.totalPages != null)
                        if ($Number(object.totalPages) !== 0)
                            message.totalPages = object.totalPages | 0;
                    if (object.totalElements != null)
                        if (typeof object.totalElements === "object" ? object.totalElements.low || object.totalElements.high : $Number(object.totalElements) !== 0)
                            if ($util.Long)
                                message.totalElements = $util.Long.fromValue(object.totalElements, false);
                            else if (typeof object.totalElements === "string")
                                message.totalElements = $parseInt(object.totalElements, 10);
                            else if (typeof object.totalElements === "number")
                                message.totalElements = object.totalElements;
                            else if (typeof object.totalElements === "object")
                                message.totalElements = new $util.LongBits(object.totalElements.low >>> 0, object.totalElements.high >>> 0).toNumber();
                    if (object.currentPage != null)
                        if ($Number(object.currentPage) !== 0)
                            message.currentPage = object.currentPage | 0;
                    if (object.queryTimeMs != null)
                        if (typeof object.queryTimeMs === "object" ? object.queryTimeMs.low || object.queryTimeMs.high : $Number(object.queryTimeMs) !== 0)
                            if ($util.Long)
                                message.queryTimeMs = $util.Long.fromValue(object.queryTimeMs, false);
                            else if (typeof object.queryTimeMs === "string")
                                message.queryTimeMs = $parseInt(object.queryTimeMs, 10);
                            else if (typeof object.queryTimeMs === "number")
                                message.queryTimeMs = object.queryTimeMs;
                            else if (typeof object.queryTimeMs === "object")
                                message.queryTimeMs = new $util.LongBits(object.queryTimeMs.low >>> 0, object.queryTimeMs.high >>> 0).toNumber();
                    return message;
                };

                /**
                 * Creates a plain object from an UeEventPageResponse message. Also converts values to other types if specified.
                 * @function toObject
                 * @memberof com.example.ue.UeEventPageResponse
                 * @static
                 * @param {com.example.ue.UeEventPageResponse} message UeEventPageResponse
                 * @param {$protobuf.IConversionOptions} [options] Conversion options
                 * @returns {Object.<string,*>} Plain object
                 */
                UeEventPageResponse.toObject = function (message, options, _depth) {
                    if (!options)
                        options = {};
                    if (_depth === $undefined)
                        _depth = 0;
                    if (_depth > $util.recursionLimit)
                        throw $Error("max depth exceeded");
                    var object = {};
                    if (options.arrays || options.defaults)
                        object.events = [];
                    if (options.defaults) {
                        object.totalPages = 0;
                        if ($util.Long) {
                            var long = new $util.Long(0, 0, false);
                            object.totalElements = options.longs === $String ? long.toString() : options.longs === $Number ? long.toNumber() : typeof $BigInt !== "undefined" && options.longs === $BigInt ? long.toBigInt() : long;
                        } else
                            object.totalElements = options.longs === $String ? "0" : typeof $BigInt !== "undefined" && options.longs === $BigInt ? $BigInt("0") : 0;
                        object.currentPage = 0;
                        if ($util.Long) {
                            var long = new $util.Long(0, 0, false);
                            object.queryTimeMs = options.longs === $String ? long.toString() : options.longs === $Number ? long.toNumber() : typeof $BigInt !== "undefined" && options.longs === $BigInt ? long.toBigInt() : long;
                        } else
                            object.queryTimeMs = options.longs === $String ? "0" : typeof $BigInt !== "undefined" && options.longs === $BigInt ? $BigInt("0") : 0;
                    }
                    if (message.events && message.events.length) {
                        object.events = $Array(message.events.length);
                        for (var j = 0; j < message.events.length; ++j)
                            object.events[j] = $root.com.example.ue.UeEvent.toObject(message.events[j], options, _depth + 1);
                    }
                    if (message.totalPages != null && $Object.hasOwnProperty.call(message, "totalPages"))
                        object.totalPages = message.totalPages;
                    if (message.totalElements != null && $Object.hasOwnProperty.call(message, "totalElements"))
                        if (typeof $BigInt !== "undefined" && options.longs === $BigInt)
                            object.totalElements = typeof message.totalElements === "number" ? $BigInt(message.totalElements) : $util.Long.fromBits(message.totalElements.low >>> 0, message.totalElements.high >>> 0, false).toBigInt();
                        else if (typeof message.totalElements === "number")
                            object.totalElements = options.longs === $String ? $String(message.totalElements) : message.totalElements;
                        else
                            object.totalElements = options.longs === $String ? $util.Long.prototype.toString.call(message.totalElements) : options.longs === $Number ? new $util.LongBits(message.totalElements.low >>> 0, message.totalElements.high >>> 0).toNumber() : message.totalElements;
                    if (message.currentPage != null && $Object.hasOwnProperty.call(message, "currentPage"))
                        object.currentPage = message.currentPage;
                    if (message.queryTimeMs != null && $Object.hasOwnProperty.call(message, "queryTimeMs"))
                        if (typeof $BigInt !== "undefined" && options.longs === $BigInt)
                            object.queryTimeMs = typeof message.queryTimeMs === "number" ? $BigInt(message.queryTimeMs) : $util.Long.fromBits(message.queryTimeMs.low >>> 0, message.queryTimeMs.high >>> 0, false).toBigInt();
                        else if (typeof message.queryTimeMs === "number")
                            object.queryTimeMs = options.longs === $String ? $String(message.queryTimeMs) : message.queryTimeMs;
                        else
                            object.queryTimeMs = options.longs === $String ? $util.Long.prototype.toString.call(message.queryTimeMs) : options.longs === $Number ? new $util.LongBits(message.queryTimeMs.low >>> 0, message.queryTimeMs.high >>> 0).toNumber() : message.queryTimeMs;
                    return object;
                };

                /**
                 * Converts this UeEventPageResponse to JSON.
                 * @function toJSON
                 * @memberof com.example.ue.UeEventPageResponse
                 * @instance
                 * @returns {Object.<string,*>} JSON object
                 */
                UeEventPageResponse.prototype.toJSON = function() {
                    return UeEventPageResponse.toObject(this, $protobuf.util.toJSONOptions);
                };

                /**
                 * Gets the type url for UeEventPageResponse
                 * @function getTypeUrl
                 * @memberof com.example.ue.UeEventPageResponse
                 * @static
                 * @param {string} [prefix] Custom type url prefix, defaults to `"type.googleapis.com"`
                 * @returns {string} The type url
                 */
                UeEventPageResponse.getTypeUrl = function(prefix) {
                    if (prefix === $undefined)
                        prefix = "type.googleapis.com";
                    return prefix + "/com.example.ue.UeEventPageResponse";
                };

                return UeEventPageResponse;
            })();

            ue.EmptyRequest = (function() {

                /**
                 * Properties of an EmptyRequest.
                 * @typedef {Object} com.example.ue.EmptyRequest.$Properties
                 * @property {Array.<Uint8Array>} [$unknowns] Unknown fields preserved while decoding when enabled
                 */

                /**
                 * Properties of an EmptyRequest.
                 * @memberof com.example.ue
                 * @interface IEmptyRequest
                 * @augments com.example.ue.EmptyRequest.$Properties
                 * @deprecated Use com.example.ue.EmptyRequest.$Properties instead.
                 */

                /**
                 * Shape of an EmptyRequest.
                 * @typedef {com.example.ue.EmptyRequest.$Properties} com.example.ue.EmptyRequest.$Shape
                 */

                /**
                 * Constructs a new EmptyRequest.
                 * @memberof com.example.ue
                 * @classdesc Represents an EmptyRequest.
                 * @constructor
                 * @param {com.example.ue.EmptyRequest.$Properties=} [properties] Properties to set
                 * @property {Array.<Uint8Array>} [$unknowns] Unknown fields preserved while decoding when enabled
                 */
                var EmptyRequest = function (properties) {
                    if (properties)
                        for (var keys = $Object.keys(properties), i = 0; i < keys.length; ++i)
                            if (properties[keys[i]] != null && keys[i] !== "__proto__")
                                this[keys[i]] = properties[keys[i]];
                };

                /**
                 * Creates a new EmptyRequest instance using the specified properties.
                 * @function create
                 * @memberof com.example.ue.EmptyRequest
                 * @static
                 * @param {com.example.ue.EmptyRequest.$Properties=} [properties] Properties to set
                 * @returns {com.example.ue.EmptyRequest} EmptyRequest instance
                 * @type {{
                 *   (properties: com.example.ue.EmptyRequest.$Shape): com.example.ue.EmptyRequest & com.example.ue.EmptyRequest.$Shape;
                 *   (properties?: com.example.ue.EmptyRequest.$Properties): com.example.ue.EmptyRequest;
                 * }}
                 */
                EmptyRequest.create = function(properties) {
                    return new EmptyRequest(properties);
                };

                /**
                 * Encodes the specified EmptyRequest message. Does not implicitly {@link com.example.ue.EmptyRequest.verify|verify} messages.
                 * @function encode
                 * @memberof com.example.ue.EmptyRequest
                 * @static
                 * @param {com.example.ue.EmptyRequest.$Properties} message EmptyRequest message or plain object to encode
                 * @param {$protobuf.Writer} [writer] Writer to encode to
                 * @returns {$protobuf.Writer} Writer
                 */
                EmptyRequest.encode = function (message, writer, _depth) {
                    if (!writer)
                        writer = $Writer.create();
                    if (_depth === $undefined)
                        _depth = 0;
                    if (_depth > $util.recursionLimit)
                        throw $Error("max depth exceeded");
                    if (message.$unknowns != null && $Object.hasOwnProperty.call(message, "$unknowns"))
                        for (var i = 0; i < message.$unknowns.length; ++i)
                            writer.raw(message.$unknowns[i]);
                    return writer;
                };

                /**
                 * Encodes the specified EmptyRequest message, length delimited. Does not implicitly {@link com.example.ue.EmptyRequest.verify|verify} messages.
                 * @function encodeDelimited
                 * @memberof com.example.ue.EmptyRequest
                 * @static
                 * @param {com.example.ue.EmptyRequest.$Properties} message EmptyRequest message or plain object to encode
                 * @param {$protobuf.Writer} [writer] Writer to encode to
                 * @returns {$protobuf.Writer} Writer
                 */
                EmptyRequest.encodeDelimited = function(message, writer) {
                    return this.encode(message, writer && writer.len ? writer.fork() : writer).ldelim();
                };

                /**
                 * Decodes an EmptyRequest message from the specified reader or buffer.
                 * @function decode
                 * @memberof com.example.ue.EmptyRequest
                 * @static
                 * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                 * @param {number} [length] Message length if known beforehand
                 * @returns {com.example.ue.EmptyRequest & com.example.ue.EmptyRequest.$Shape} EmptyRequest
                 * @throws {Error} If the payload is not a reader or valid buffer
                 * @throws {$protobuf.util.ProtocolError} If required fields are missing
                 */
                EmptyRequest.decode = function (reader, length, _end, _depth, _target) {
                    if (!(reader instanceof $Reader))
                        reader = $Reader.create(reader);
                    if (_depth === $undefined)
                        _depth = 0;
                    if (_depth > $Reader.recursionLimit)
                        throw $Error("max depth exceeded");
                    var end = length === $undefined ? reader.len : reader.pos + length, message = _target || new $root.com.example.ue.EmptyRequest();
                    while (reader.pos < end) {
                        var start = reader.pos;
                        var tag = reader.tag();
                        if (tag === _end) {
                            _end = $undefined;
                            break;
                        }
                        reader.skipType(tag & 7, _depth, tag);
                        if (!reader.discardUnknown) {
                            $util.makeProp(message, "$unknowns", false);
                            (message.$unknowns || (message.$unknowns = [])).push(reader.raw(start, reader.pos));
                        }
                    }
                    if (_end !== $undefined)
                        throw $Error("missing end group");
                    return message;
                };

                /**
                 * Decodes an EmptyRequest message from the specified reader or buffer, length delimited.
                 * @function decodeDelimited
                 * @memberof com.example.ue.EmptyRequest
                 * @static
                 * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                 * @returns {com.example.ue.EmptyRequest & com.example.ue.EmptyRequest.$Shape} EmptyRequest
                 * @throws {Error} If the payload is not a reader or valid buffer
                 * @throws {$protobuf.util.ProtocolError} If required fields are missing
                 */
                EmptyRequest.decodeDelimited = function(reader) {
                    if (!(reader instanceof $Reader))
                        reader = new $Reader(reader);
                    return this.decode(reader, reader.uint32());
                };

                /**
                 * Verifies an EmptyRequest message.
                 * @function verify
                 * @memberof com.example.ue.EmptyRequest
                 * @static
                 * @param {Object.<string,*>} message Plain object to verify
                 * @returns {string|null} `null` if valid, otherwise the reason why it is not
                 */
                EmptyRequest.verify = function (message, _depth) {
                    if (typeof message !== "object" || message === null)
                        return "object expected";
                    if (_depth === $undefined)
                        _depth = 0;
                    if (_depth > $util.recursionLimit)
                        return "max depth exceeded";
                    return null;
                };

                /**
                 * Creates an EmptyRequest message from a plain object. Also converts values to their respective internal types.
                 * @function fromObject
                 * @memberof com.example.ue.EmptyRequest
                 * @static
                 * @param {Object.<string,*>} object Plain object
                 * @returns {com.example.ue.EmptyRequest} EmptyRequest
                 */
                EmptyRequest.fromObject = function (object, _depth) {
                    if (object instanceof $root.com.example.ue.EmptyRequest)
                        return object;
                    if (!$util.isObject(object))
                        throw $TypeError(".com.example.ue.EmptyRequest: object expected");
                    if (_depth === $undefined)
                        _depth = 0;
                    if (_depth > $util.recursionLimit)
                        throw $Error("max depth exceeded");
                    return new $root.com.example.ue.EmptyRequest();
                };

                /**
                 * Creates a plain object from an EmptyRequest message. Also converts values to other types if specified.
                 * @function toObject
                 * @memberof com.example.ue.EmptyRequest
                 * @static
                 * @param {com.example.ue.EmptyRequest} message EmptyRequest
                 * @param {$protobuf.IConversionOptions} [options] Conversion options
                 * @returns {Object.<string,*>} Plain object
                 */
                EmptyRequest.toObject = function () {
                    return {};
                };

                /**
                 * Converts this EmptyRequest to JSON.
                 * @function toJSON
                 * @memberof com.example.ue.EmptyRequest
                 * @instance
                 * @returns {Object.<string,*>} JSON object
                 */
                EmptyRequest.prototype.toJSON = function() {
                    return EmptyRequest.toObject(this, $protobuf.util.toJSONOptions);
                };

                /**
                 * Gets the type url for EmptyRequest
                 * @function getTypeUrl
                 * @memberof com.example.ue.EmptyRequest
                 * @static
                 * @param {string} [prefix] Custom type url prefix, defaults to `"type.googleapis.com"`
                 * @returns {string} The type url
                 */
                EmptyRequest.getTypeUrl = function(prefix) {
                    if (prefix === $undefined)
                        prefix = "type.googleapis.com";
                    return prefix + "/com.example.ue.EmptyRequest";
                };

                return EmptyRequest;
            })();

            return ue;
        })();

        return example;
    })();

    return com;
})();

module.exports = $root;
