import * as $protobuf from "protobufjs";
import Long = require("long");

/** Namespace com. */
export namespace com {

    /** Namespace example. */
    namespace example {

        /** Namespace ue. */
        namespace ue {

            /** ActionTaken enum. */
            enum ActionTaken {

                /** UNKNOWN_ACTION value */
                UNKNOWN_ACTION = 0,

                /** REJECT value */
                REJECT = 1,

                /** ATTACH value */
                ATTACH = 2,

                /** SILENT_CALL value */
                SILENT_CALL = 3,

                /** DETACH value */
                DETACH = 4,

                /** LOCATION_UPDATE value */
                LOCATION_UPDATE = 5,

                /** PAGING value */
                PAGING = 6
            }

            /** RatType enum. */
            enum RatType {

                /** UNKNOWN_RAT value */
                UNKNOWN_RAT = 0,

                /** RAT_2G value */
                RAT_2G = 1,

                /** RAT_3G value */
                RAT_3G = 2,

                /** RAT_4G_LTE value */
                RAT_4G_LTE = 3,

                /** RAT_5G value */
                RAT_5G = 4
            }

            /**
             * Properties of an UeEvent.
             * @deprecated Use com.example.ue.UeEvent.$Properties instead.
             */
            interface IUeEvent extends com.example.ue.UeEvent.$Properties {
            }

            /** Represents an UeEvent. */
            class UeEvent {

                /**
                 * Constructs a new UeEvent.
                 * @param [properties] Properties to set
                 */
                constructor(properties?: com.example.ue.UeEvent.$Properties);

                /** Unknown fields preserved while decoding when enabled */
                $unknowns?: Uint8Array[];

                /** UeEvent imsiOrSupi. */
                imsiOrSupi: string;

                /** UeEvent imei. */
                imei: string;

                /** UeEvent msisdn. */
                msisdn: string;

                /** UeEvent guti. */
                guti: string;

                /** UeEvent tmsi. */
                tmsi: string;

                /** UeEvent rssi. */
                rssi: number;

                /** UeEvent actionTaken. */
                actionTaken: com.example.ue.ActionTaken;

                /** UeEvent rejectCause. */
                rejectCause: number;

                /** UeEvent rat. */
                rat: com.example.ue.RatType;

                /** UeEvent frequencyBand. */
                frequencyBand: number;

                /** UeEvent arfcn. */
                arfcn: number;

                /** UeEvent trackingAreaCode. */
                trackingAreaCode: number;

                /** UeEvent downlinkBandWidth. */
                downlinkBandWidth: string;

                /** UeEvent plmnMcc. */
                plmnMcc: number;

                /** UeEvent plmnMnc. */
                plmnMnc: number;

                /** UeEvent providerName. */
                providerName: string;

                /** UeEvent missionId. */
                missionId: string;

                /** UeEvent sensorId. */
                sensorId: string;

                /** UeEvent subsystemId. */
                subsystemId: string;

                /** UeEvent trxCommandId. */
                trxCommandId: string;

                /** UeEvent createdAt. */
                createdAt: string;

                /** UeEvent updatedAt. */
                updatedAt: string;

                /** UeEvent countryIsoAlpha2. */
                countryIsoAlpha2: string;

                /** UeEvent countryName. */
                countryName: string;

                /** UeEvent target. */
                target: boolean;

                /** UeEvent captureCount. */
                captureCount: number;

                /** UeEvent timingAdvance. */
                timingAdvance: number;

                /** UeEvent distanceInMeters. */
                distanceInMeters: number;

                /**
                 * Creates a new UeEvent instance using the specified properties.
                 * @param [properties] Properties to set
                 * @returns UeEvent instance
                 */
                static create(properties: com.example.ue.UeEvent.$Shape): com.example.ue.UeEvent & com.example.ue.UeEvent.$Shape;
                static create(properties?: com.example.ue.UeEvent.$Properties): com.example.ue.UeEvent;

                /**
                 * Encodes the specified UeEvent message. Does not implicitly {@link com.example.ue.UeEvent.verify|verify} messages.
                 * @param message UeEvent message or plain object to encode
                 * @param [writer] Writer to encode to
                 * @returns Writer
                 */
                static encode(message: com.example.ue.UeEvent.$Properties, writer?: $protobuf.Writer): $protobuf.Writer;

                /**
                 * Encodes the specified UeEvent message, length delimited. Does not implicitly {@link com.example.ue.UeEvent.verify|verify} messages.
                 * @param message UeEvent message or plain object to encode
                 * @param [writer] Writer to encode to
                 * @returns Writer
                 */
                static encodeDelimited(message: com.example.ue.UeEvent.$Properties, writer?: $protobuf.Writer): $protobuf.Writer;

                /**
                 * Decodes an UeEvent message from the specified reader or buffer.
                 * @param reader Reader or buffer to decode from
                 * @param [length] Message length if known beforehand
                 * @returns {com.example.ue.UeEvent & com.example.ue.UeEvent.$Shape} UeEvent
                 * @throws {Error} If the payload is not a reader or valid buffer
                 * @throws {$protobuf.util.ProtocolError} If required fields are missing
                 */
                static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): com.example.ue.UeEvent & com.example.ue.UeEvent.$Shape;

                /**
                 * Decodes an UeEvent message from the specified reader or buffer, length delimited.
                 * @param reader Reader or buffer to decode from
                 * @returns {com.example.ue.UeEvent & com.example.ue.UeEvent.$Shape} UeEvent
                 * @throws {Error} If the payload is not a reader or valid buffer
                 * @throws {$protobuf.util.ProtocolError} If required fields are missing
                 */
                static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): com.example.ue.UeEvent & com.example.ue.UeEvent.$Shape;

                /**
                 * Verifies an UeEvent message.
                 * @param message Plain object to verify
                 * @returns `null` if valid, otherwise the reason why it is not
                 */
                static verify(message: { [k: string]: any }): (string|null);

                /**
                 * Creates an UeEvent message from a plain object. Also converts values to their respective internal types.
                 * @param object Plain object
                 * @returns UeEvent
                 */
                static fromObject(object: { [k: string]: any }): com.example.ue.UeEvent;

                /**
                 * Creates a plain object from an UeEvent message. Also converts values to other types if specified.
                 * @param message UeEvent
                 * @param [options] Conversion options
                 * @returns Plain object
                 */
                static toObject(message: com.example.ue.UeEvent, options?: $protobuf.IConversionOptions): { [k: string]: any };

                /**
                 * Converts this UeEvent to JSON.
                 * @returns JSON object
                 */
                toJSON(): { [k: string]: any };

                /**
                 * Gets the type url for UeEvent
                 * @param [prefix] Custom type url prefix, defaults to `"type.googleapis.com"`
                 * @returns The type url
                 */
                static getTypeUrl(prefix?: string): string;
            }

            namespace UeEvent {

                /** Properties of an UeEvent. */
                interface $Properties {

                    /** UeEvent imsiOrSupi */
                    imsiOrSupi?: (string|null);

                    /** UeEvent imei */
                    imei?: (string|null);

                    /** UeEvent msisdn */
                    msisdn?: (string|null);

                    /** UeEvent guti */
                    guti?: (string|null);

                    /** UeEvent tmsi */
                    tmsi?: (string|null);

                    /** UeEvent rssi */
                    rssi?: (number|null);

                    /** UeEvent actionTaken */
                    actionTaken?: (com.example.ue.ActionTaken|null);

                    /** UeEvent rejectCause */
                    rejectCause?: (number|null);

                    /** UeEvent rat */
                    rat?: (com.example.ue.RatType|null);

                    /** UeEvent frequencyBand */
                    frequencyBand?: (number|null);

                    /** UeEvent arfcn */
                    arfcn?: (number|null);

                    /** UeEvent trackingAreaCode */
                    trackingAreaCode?: (number|null);

                    /** UeEvent downlinkBandWidth */
                    downlinkBandWidth?: (string|null);

                    /** UeEvent plmnMcc */
                    plmnMcc?: (number|null);

                    /** UeEvent plmnMnc */
                    plmnMnc?: (number|null);

                    /** UeEvent providerName */
                    providerName?: (string|null);

                    /** UeEvent missionId */
                    missionId?: (string|null);

                    /** UeEvent sensorId */
                    sensorId?: (string|null);

                    /** UeEvent subsystemId */
                    subsystemId?: (string|null);

                    /** UeEvent trxCommandId */
                    trxCommandId?: (string|null);

                    /** UeEvent createdAt */
                    createdAt?: (string|null);

                    /** UeEvent updatedAt */
                    updatedAt?: (string|null);

                    /** UeEvent countryIsoAlpha2 */
                    countryIsoAlpha2?: (string|null);

                    /** UeEvent countryName */
                    countryName?: (string|null);

                    /** UeEvent target */
                    target?: (boolean|null);

                    /** UeEvent captureCount */
                    captureCount?: (number|null);

                    /** UeEvent timingAdvance */
                    timingAdvance?: (number|null);

                    /** UeEvent distanceInMeters */
                    distanceInMeters?: (number|null);

                    /** Unknown fields preserved while decoding when enabled */
                    $unknowns?: Uint8Array[];
                }

                /** Shape of an UeEvent. */
                type $Shape = com.example.ue.UeEvent.$Properties;
            }

            /**
             * Properties of an UeEventPageResponse.
             * @deprecated Use com.example.ue.UeEventPageResponse.$Properties instead.
             */
            interface IUeEventPageResponse extends com.example.ue.UeEventPageResponse.$Properties {
            }

            /** Represents an UeEventPageResponse. */
            class UeEventPageResponse {

                /**
                 * Constructs a new UeEventPageResponse.
                 * @param [properties] Properties to set
                 */
                constructor(properties?: com.example.ue.UeEventPageResponse.$Properties);

                /** Unknown fields preserved while decoding when enabled */
                $unknowns?: Uint8Array[];

                /** UeEventPageResponse events. */
                events: com.example.ue.UeEvent.$Properties[];

                /** UeEventPageResponse totalPages. */
                totalPages: number;

                /** UeEventPageResponse totalElements. */
                totalElements: (number|Long);

                /** UeEventPageResponse currentPage. */
                currentPage: number;

                /** UeEventPageResponse queryTimeMs. */
                queryTimeMs: (number|Long);

                /** UeEventPageResponse nextCursor. */
                nextCursor: string;

                /** UeEventPageResponse hasNext. */
                hasNext: boolean;

                /**
                 * Creates a new UeEventPageResponse instance using the specified properties.
                 * @param [properties] Properties to set
                 * @returns UeEventPageResponse instance
                 */
                static create(properties: com.example.ue.UeEventPageResponse.$Shape): com.example.ue.UeEventPageResponse & com.example.ue.UeEventPageResponse.$Shape;
                static create(properties?: com.example.ue.UeEventPageResponse.$Properties): com.example.ue.UeEventPageResponse;

                /**
                 * Encodes the specified UeEventPageResponse message. Does not implicitly {@link com.example.ue.UeEventPageResponse.verify|verify} messages.
                 * @param message UeEventPageResponse message or plain object to encode
                 * @param [writer] Writer to encode to
                 * @returns Writer
                 */
                static encode(message: com.example.ue.UeEventPageResponse.$Properties, writer?: $protobuf.Writer): $protobuf.Writer;

                /**
                 * Encodes the specified UeEventPageResponse message, length delimited. Does not implicitly {@link com.example.ue.UeEventPageResponse.verify|verify} messages.
                 * @param message UeEventPageResponse message or plain object to encode
                 * @param [writer] Writer to encode to
                 * @returns Writer
                 */
                static encodeDelimited(message: com.example.ue.UeEventPageResponse.$Properties, writer?: $protobuf.Writer): $protobuf.Writer;

                /**
                 * Decodes an UeEventPageResponse message from the specified reader or buffer.
                 * @param reader Reader or buffer to decode from
                 * @param [length] Message length if known beforehand
                 * @returns {com.example.ue.UeEventPageResponse & com.example.ue.UeEventPageResponse.$Shape} UeEventPageResponse
                 * @throws {Error} If the payload is not a reader or valid buffer
                 * @throws {$protobuf.util.ProtocolError} If required fields are missing
                 */
                static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): com.example.ue.UeEventPageResponse & com.example.ue.UeEventPageResponse.$Shape;

                /**
                 * Decodes an UeEventPageResponse message from the specified reader or buffer, length delimited.
                 * @param reader Reader or buffer to decode from
                 * @returns {com.example.ue.UeEventPageResponse & com.example.ue.UeEventPageResponse.$Shape} UeEventPageResponse
                 * @throws {Error} If the payload is not a reader or valid buffer
                 * @throws {$protobuf.util.ProtocolError} If required fields are missing
                 */
                static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): com.example.ue.UeEventPageResponse & com.example.ue.UeEventPageResponse.$Shape;

                /**
                 * Verifies an UeEventPageResponse message.
                 * @param message Plain object to verify
                 * @returns `null` if valid, otherwise the reason why it is not
                 */
                static verify(message: { [k: string]: any }): (string|null);

                /**
                 * Creates an UeEventPageResponse message from a plain object. Also converts values to their respective internal types.
                 * @param object Plain object
                 * @returns UeEventPageResponse
                 */
                static fromObject(object: { [k: string]: any }): com.example.ue.UeEventPageResponse;

                /**
                 * Creates a plain object from an UeEventPageResponse message. Also converts values to other types if specified.
                 * @param message UeEventPageResponse
                 * @param [options] Conversion options
                 * @returns Plain object
                 */
                static toObject(message: com.example.ue.UeEventPageResponse, options?: $protobuf.IConversionOptions): { [k: string]: any };

                /**
                 * Converts this UeEventPageResponse to JSON.
                 * @returns JSON object
                 */
                toJSON(): { [k: string]: any };

                /**
                 * Gets the type url for UeEventPageResponse
                 * @param [prefix] Custom type url prefix, defaults to `"type.googleapis.com"`
                 * @returns The type url
                 */
                static getTypeUrl(prefix?: string): string;
            }

            namespace UeEventPageResponse {

                /** Properties of an UeEventPageResponse. */
                interface $Properties {

                    /** UeEventPageResponse events */
                    events?: (com.example.ue.UeEvent.$Properties[]|null);

                    /** UeEventPageResponse totalPages */
                    totalPages?: (number|null);

                    /** UeEventPageResponse totalElements */
                    totalElements?: (number|Long|null);

                    /** UeEventPageResponse currentPage */
                    currentPage?: (number|null);

                    /** UeEventPageResponse queryTimeMs */
                    queryTimeMs?: (number|Long|null);

                    /** UeEventPageResponse nextCursor */
                    nextCursor?: (string|null);

                    /** UeEventPageResponse hasNext */
                    hasNext?: (boolean|null);

                    /** Unknown fields preserved while decoding when enabled */
                    $unknowns?: Uint8Array[];
                }

                /** Shape of an UeEventPageResponse. */
                type $Shape = com.example.ue.UeEventPageResponse.$Properties;
            }

            /**
             * Properties of an EmptyRequest.
             * @deprecated Use com.example.ue.EmptyRequest.$Properties instead.
             */
            interface IEmptyRequest extends com.example.ue.EmptyRequest.$Properties {
            }

            /** Represents an EmptyRequest. */
            class EmptyRequest {

                /**
                 * Constructs a new EmptyRequest.
                 * @param [properties] Properties to set
                 */
                constructor(properties?: com.example.ue.EmptyRequest.$Properties);

                /** Unknown fields preserved while decoding when enabled */
                $unknowns?: Uint8Array[];

                /**
                 * Creates a new EmptyRequest instance using the specified properties.
                 * @param [properties] Properties to set
                 * @returns EmptyRequest instance
                 */
                static create(properties: com.example.ue.EmptyRequest.$Shape): com.example.ue.EmptyRequest & com.example.ue.EmptyRequest.$Shape;
                static create(properties?: com.example.ue.EmptyRequest.$Properties): com.example.ue.EmptyRequest;

                /**
                 * Encodes the specified EmptyRequest message. Does not implicitly {@link com.example.ue.EmptyRequest.verify|verify} messages.
                 * @param message EmptyRequest message or plain object to encode
                 * @param [writer] Writer to encode to
                 * @returns Writer
                 */
                static encode(message: com.example.ue.EmptyRequest.$Properties, writer?: $protobuf.Writer): $protobuf.Writer;

                /**
                 * Encodes the specified EmptyRequest message, length delimited. Does not implicitly {@link com.example.ue.EmptyRequest.verify|verify} messages.
                 * @param message EmptyRequest message or plain object to encode
                 * @param [writer] Writer to encode to
                 * @returns Writer
                 */
                static encodeDelimited(message: com.example.ue.EmptyRequest.$Properties, writer?: $protobuf.Writer): $protobuf.Writer;

                /**
                 * Decodes an EmptyRequest message from the specified reader or buffer.
                 * @param reader Reader or buffer to decode from
                 * @param [length] Message length if known beforehand
                 * @returns {com.example.ue.EmptyRequest & com.example.ue.EmptyRequest.$Shape} EmptyRequest
                 * @throws {Error} If the payload is not a reader or valid buffer
                 * @throws {$protobuf.util.ProtocolError} If required fields are missing
                 */
                static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): com.example.ue.EmptyRequest & com.example.ue.EmptyRequest.$Shape;

                /**
                 * Decodes an EmptyRequest message from the specified reader or buffer, length delimited.
                 * @param reader Reader or buffer to decode from
                 * @returns {com.example.ue.EmptyRequest & com.example.ue.EmptyRequest.$Shape} EmptyRequest
                 * @throws {Error} If the payload is not a reader or valid buffer
                 * @throws {$protobuf.util.ProtocolError} If required fields are missing
                 */
                static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): com.example.ue.EmptyRequest & com.example.ue.EmptyRequest.$Shape;

                /**
                 * Verifies an EmptyRequest message.
                 * @param message Plain object to verify
                 * @returns `null` if valid, otherwise the reason why it is not
                 */
                static verify(message: { [k: string]: any }): (string|null);

                /**
                 * Creates an EmptyRequest message from a plain object. Also converts values to their respective internal types.
                 * @param object Plain object
                 * @returns EmptyRequest
                 */
                static fromObject(object: { [k: string]: any }): com.example.ue.EmptyRequest;

                /**
                 * Creates a plain object from an EmptyRequest message. Also converts values to other types if specified.
                 * @param message EmptyRequest
                 * @param [options] Conversion options
                 * @returns Plain object
                 */
                static toObject(message: com.example.ue.EmptyRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

                /**
                 * Converts this EmptyRequest to JSON.
                 * @returns JSON object
                 */
                toJSON(): { [k: string]: any };

                /**
                 * Gets the type url for EmptyRequest
                 * @param [prefix] Custom type url prefix, defaults to `"type.googleapis.com"`
                 * @returns The type url
                 */
                static getTypeUrl(prefix?: string): string;
            }

            namespace EmptyRequest {

                /** Properties of an EmptyRequest. */
                interface $Properties {

                    /** Unknown fields preserved while decoding when enabled */
                    $unknowns?: Uint8Array[];
                }

                /** Shape of an EmptyRequest. */
                type $Shape = com.example.ue.EmptyRequest.$Properties;
            }
        }
    }
}
