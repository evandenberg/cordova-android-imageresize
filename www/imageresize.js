/*global module,window,cordova,require */

/**
 * An Image Resizer Plugin for PhoneGap. Updated to fit Cordova 2+
 * 
 * The software is open source, MIT licensed.
 * Copyright (C) 2012, webXells GmbH All Rights Reserved.
 * 
 * Raanan Weber, webXells GmbH http://www.webxells.com
 */
var cordova = require('cordova'),
    exec = require('cordova/exec'),
    ImageResizerConst = require('./ImageResizer'),
    imageResizer;

(function () {
    'use strict';

    var ImageResize = function() {

    };

    ImageResizerConst.IMAGE_DATA_TYPE_BASE64 = "base64Image";
    ImageResizerConst.IMAGE_DATA_TYPE_URL = "urlImage";
    ImageResizerConst.RESIZE_TYPE_FACTOR = "factorResize";
    ImageResizerConst.RESIZE_TYPE_PIXEL = "pixelResize";
    ImageResizerConst.FORMAT_JPG = "jpg";
    ImageResizerConst.FORMAT_PNG = "png";
    ImageResizerConst.FORMAT_PDF = "pdf";

    /**
     * Resize an image
     * @param success success callback, will receive the data sent from the native plugin
     * @param fail error callback, will receive an error string describing what went wrong
     * @param imageData The image data, either base64 or local url
     * @param width width factor / width in pixels
     * @param height height factor / height in pixels
     * @param options extra options -  
     *              format : file format to use (ImageResizer.FORMAT_JPG/ImageResizer.FORMAT_PNG) - defaults to JPG
     *              imageDataType : the data type (IMAGE_DATA_TYPE_BASE64/IMAGE_DATA_TYPE_URL) - defaults to Base64
     *              resizeType : type of the resize (RESIZE_TYPE_FACTOR/RESIZE_TYPE_PIXEL) - must be given
     *              quality : INTEGER, compression quality - defaults to 70
     * @returns JSON Object with the following parameters:
     *              imageData : Base64 of the resized image
     *              height : height of the resized image
     *              width: width of the resized image
     */
    ImageResize.prototype.resizeImage = function(success, fail, imageData, width,
            height, options) {
        if (!options) {
            options = {};
        }

        var params = {
            data : imageData || '',
            width : width,
            height : height,
            format : options.format,
            imageDataType : options.imageDataType || ImageResizerConst.IMAGE_DATA_TYPE_BASE64,
            filename : options.filename || '',
            directory : options.directory || '',
            resizeType : options.resizeType,
            quality : options.quality || 70
        };
        //console.log('resizeImage Called: '+JSON.stringify(params));
        return exec(success, fail, "ImageResize", "resizeImage", [params]);
    };
    /**
     * Get an image width and height
     * @param success success callback, will receive the data sent from the native plugin
     * @param fail error callback, will receive an error string describing what went wrong
     * @param imageData The image data, either base64 or local url
     * @param options extra options -  
     *              imageDataType : the data type (IMAGE_DATA_TYPE_BASE64/IMAGE_DATA_TYPE_URL) - defaults to Base64
     * @returns JSON Object with the following parameters:
     *              height : height of the image
     *              width: width of the image
     */
    ImageResize.prototype.getImageSize = function(success, fail, imageData,
            options) {
        if (!options) {
            options = {};
        }
        var params = {
            data : imageData,
            imageDataType : options.imageType 
        };
        return exec(success, fail, "ImageResize",
                "imageSize", [params]);
    };

    /**
     * Store an image locally
     * @param success success callback, will receive the data sent from the native plugin
     * @param fail error callback, will receive an error string describing what went wrong
     * @param imageData The image data, either base64 or local url
     * @param options extra options -  
     *              format : file format to use (ImageResizer.FORMAT_JPG/ImageResizer.FORMAT_PNG) - defaults to JPG
     *              imageDataType : the data type (IMAGE_DATA_TYPE_BASE64/IMAGE_DATA_TYPE_URL) - defaults to Base64
     *              filename : filename to be stored, with ot without ending (if no ending given, format will be used) - must be given.
     *              directory : in which directory should the file be stored - must be given
     *              quality : INTEGER, compression quality - defaults to 100
     *				photoAlbum : [iOS only] store the image in the temporary directory of the app, or in the photoAlbum (true for photoAlbum)
     *							 Note : in iOS only filename should be given, directory will be ignored.
     * @returns JSON Object with the following parameters:
     *              url : URL of the file just stored
     */
    ImageResize.prototype.storeImage = function(success, fail, imageData, options) {
        if (!options) {
            options = {};
        }
        var params = {
            data : imageData,
            format : options.format,
            imageDataType : options.imageType,
            filename : options.filename,
            directory : options.directory,
            quality : options.quality || 100,
    		photoAlbum : options.photoAlbum || true
        };

        return exec(success, fail, "ImageResize",
                "storeImage", [params]);
    };

    ImageResize.prototype.storePDF = function(success, fail, imageData, options) {
        if (!options) {
            options = {};
        }
        var params = {
            data : imageData,
            format : options.format,
            imageDataType : options.imageType,
            filename : options.filename,
            directory : options.directory,
            quality : options.quality || 100,
            photoAlbum : options.photoAlbum || true
        };

        return exec(success, fail, "ImageResize",
                "storePDF", [params]);
    };

    imageResizer = new ImageResize();

    module.exports = imageResizer;

}());
